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
        System.out.println("Adding user to group - userId: " + userId + ", groupId: " + groupId + ", sessionId: " + sessionId);
        groupSessions.computeIfAbsent(groupId, k -> new ConcurrentHashMap<>())
                .put(sessionId, userId);
        
        userSessions.put(userId, sessionId);
        System.out.println("Current group sessions: " + groupSessions);
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
    
    public void sendTimerUpdate(String userId, String username, String groupId, String status, String duration, String startTime) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "TIMER_UPDATE");
        data.put("userId", userId);
        data.put("username", username);
        data.put("groupId", groupId);
        data.put("status", status);
        data.put("duration", duration);
        data.put("startTime", startTime);
        
        String destination = "/topic/group." + groupId;
        messagingTemplate.convertAndSend(destination, data);
    }

    public void sendMessageToGroup(String groupId, Map<String, Object> message) {
        System.out.println("Sending message to group " + groupId + ": " + message);
        System.out.println("Current group sessions: " + groupSessions);
        System.out.println("Group " + groupId + " members: " + (groupSessions.get(groupId) != null ? groupSessions.get(groupId).values() : "no members"));
        
        String destination = "/topic/group." + groupId;
        try {
            messagingTemplate.convertAndSend(destination, message);
            System.out.println("Successfully sent message to " + destination);
        } catch (Exception e) {
            System.err.println("Error sending message to " + destination + ": " + e.getMessage());
            e.printStackTrace();
        }
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