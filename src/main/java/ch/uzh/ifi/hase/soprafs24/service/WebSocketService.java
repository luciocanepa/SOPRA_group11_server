package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, Map<String, String>> groupSessions = new ConcurrentHashMap<>();
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    @Autowired
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void addUserToGroup(String groupId, String sessionId, String userId) {
        groupSessions.computeIfAbsent(groupId, k -> new ConcurrentHashMap<>())
                .put(sessionId, userId);
        
        userSessions.put(userId, sessionId);
    }

    public void removeUserFromGroup(String groupId, String sessionId) {
        Map<String, String> group = groupSessions.get(groupId);
        if (group != null) {
            String userId = group.remove(sessionId);
            if (group.isEmpty()) {
                groupSessions.remove(groupId);
            }
            
            if (userId != null) {
                userSessions.remove(userId);
            }
        }
    }
    
    public void removeUserFromGroupByUserId(String groupId, String userId) {
        String sessionId = userSessions.get(userId);
        
        if (sessionId != null) {
            removeUserFromGroup(groupId, sessionId);
        }
    }

    public void sendMessageToGroup(String groupId, Map<String, Object> message) {
        messagingTemplate.convertAndSend("/topic/group." + groupId, message);
    }
    
    /**
     * Sends a message to a specific topic
     * 
     * @param topic The topic to send the message to
     * @param payload The message payload
     */
    public void sendMessageToTopic(String topic, Object payload) {
        messagingTemplate.convertAndSend(topic, payload);
    }
    
    public void notifyGroupMembers(User user, Long groupId) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "GROUP_UPDATE");
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("status", user.getStatus());
        data.put("message", String.format("User %s has joined the group", user.getUsername()));
        
        messagingTemplate.convertAndSend("/topic/group." + groupId, data);
    }
    
    public void notifyGroupLeave(User user, Long groupId) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "GROUP_UPDATE");
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("status", user.getStatus());
        data.put("message", String.format("User %s has left the group", user.getUsername()));
        
        messagingTemplate.convertAndSend("/topic/group." + groupId, data);
    }
    
    public void sendTimerUpdate(String userId, String username, String status, long duration, String startTime) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "TIMER_UPDATE");
        data.put("userId", userId);
        data.put("username", username);
        data.put("status", status);
        data.put("duration", duration);
        data.put("startTime", startTime);
        
        // Send to timer topic only
        messagingTemplate.convertAndSend("/topic/timer", data);
    }

    /**
     * Gets all groups a user is a member of
     * 
     * @param userId The user ID
     * @return Set of group IDs the user is a member of
     */
    public Set<String> getUserGroups(String userId) {
        Set<String> groups = new HashSet<>();
        groupSessions.forEach((groupId, members) -> {
            if (members.containsValue(userId)) {
                groups.add(groupId);
            }
        });
        return groups;
    }
} 