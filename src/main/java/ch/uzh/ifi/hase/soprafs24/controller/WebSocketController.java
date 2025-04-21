package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import ch.uzh.ifi.hase.soprafs24.service.WebSocketService;
import ch.uzh.ifi.hase.soprafs24.service.GroupService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.TimerUpdate;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Controller
public class WebSocketController {
    
    private final WebSocketService webSocketService;
    private final GroupService groupService;
    private final UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(WebSocketService webSocketService, 
                             GroupService groupService,
                             UserService userService) {
        this.webSocketService = webSocketService;
        this.groupService = groupService;
        this.userService = userService;
    }

    /**
     * Handles subscription to a group topic
     * This method is called when a client subscribes to a group topic
     * 
     * @param groupId The ID of the group to subscribe to
     * @return A message confirming the subscription
     */
    @MessageMapping("/topic/group/{groupId}")
    public String handleSubscription(Long groupId, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String userId = headerAccessor.getUser().getName();
        webSocketService.addUserToGroup(groupId.toString(), sessionId, userId);
        
        return String.format("User with ID %s subscribed to group %d", userId, groupId);
    }

    /**
     * Handles a user joining a group
     * This method is called when a user is added to a group in the database
     * 
     * @param payload Contains userId and groupId
     */
    @MessageMapping("/group.join")
    public String handleGroupJoin(@Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        String groupId = payload.get("groupId");
        
        try {
            User user = userService.getUserById(Long.parseLong(userId), "");
            
            if (payload.containsKey("sessionId")) {
                String sessionId = payload.get("sessionId");
                webSocketService.addUserToGroup(groupId, sessionId, userId);
            }
            
            webSocketService.notifyGroupMembers(user, Long.parseLong(groupId));

            return String.format("User with ID %s joined group %d", userId, groupId);
        } catch (Exception e) {
            return String.format("Error handling group join: %s", e.getMessage());
        }
    }

    /**
     * Handles a user leaving a group
     * This method is called when a user is removed from a group in the database
     * 
     * @param payload Contains userId and groupId
     */
    @MessageMapping("/group.leave")
    public String handleGroupLeave(@Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        String groupId = payload.get("groupId");
        
        try {
            User user = userService.getUserById(Long.parseLong(userId), "");
            
            webSocketService.removeUserFromGroupByUserId(groupId, userId);
            webSocketService.notifyGroupLeave(user, Long.parseLong(groupId));

            return String.format("User with ID %s left group %d", userId, groupId);
        } catch (Exception e) {
            return String.format("Error handling group leave: %s", e.getMessage());
        }
    }

    /**
     * Handles timer updates from users
     * 
     * @param timerUpdate Contains userId, status, duration, and startTime
     */
    @MessageMapping("/timer.update")
    public String handleTimerUpdate(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String userId;
        try {
            userId = null;
            if (headerAccessor.getUser() != null) {
                userId = headerAccessor.getUser().getName();
            } else if (payload.containsKey("userId")) {
                userId = payload.get("userId").toString();
            } else {
                return String.format("No userId found in request");
            }
            
            TimerUpdate timerUpdate = new TimerUpdate();
            timerUpdate.setUserId(userId);
            timerUpdate.setStatus((String) payload.get("status"));
            timerUpdate.setDuration(((Number) payload.get("duration")).longValue());
            timerUpdate.setStartTime((String) payload.get("startTime"));

            try {
                User user = userService.getUserById(Long.parseLong(userId), "");
                timerUpdate.setUsername(user.getUsername());
            } catch (Exception e) {
                return String.format("Error getting user for timer update: %s", e.getMessage());
            }

            webSocketService.sendMessageToTopic("/topic/timer", timerUpdate);

            return String.format("Timer update sent to topic /topic/timer");
        } catch (Exception e) {
            return String.format("Error handling timer update: %s", e.getMessage());
        }
    }

    /**
     * Handles subscription to a group topic
     * This method is called when a client subscribes to a group topic
     * 
     * @return A welcome message
     */
    @SubscribeMapping("/topic/group/{groupId}")
    public String handleGroupSubscription() {
        return "Welcome to the group!";
    }
} 