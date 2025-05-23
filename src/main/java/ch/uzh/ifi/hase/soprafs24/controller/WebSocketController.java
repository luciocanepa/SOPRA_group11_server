package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;


import ch.uzh.ifi.hase.soprafs24.service.WebSocketService;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.GroupService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;

import java.util.Map;
//import java.time.LocalDateTime;
import java.time.Instant;

@Controller
@Transactional
public class WebSocketController {
    
    private final WebSocketService webSocketService;
    private final GroupService groupService;
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(WebSocketService webSocketService, 
                             GroupService groupService,
                             UserService userService,
                             AuthService authService) {
        this.webSocketService = webSocketService;
        this.groupService = groupService;
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Handles a user joining a group
     * This method is called when a user is added to a group in the database
     * 
     * @param payload Contains userId and groupId
     */
    @MessageMapping("/group.join")
    public String handleGroupJoin(@Payload Map<String, String> payload, 
                                SimpMessageHeaderAccessor headerAccessor,
                                @Header("Authorization") String token) {
        String userId = payload.get("userId");
        String groupId = payload.get("groupId");
        String sessionId = headerAccessor.getSessionId();
        
        authService.authCheck(userId, groupId, token);
        
        try {
            webSocketService.addUserToGroup(groupId, sessionId, userId);
            return String.format("User with ID %s joined group %s", userId, groupId);
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
    public String handleGroupLeave(@Payload Map<String, String> payload,
                                 @Header("Authorization") String token) {
        String userId = payload.get("userId");
        String groupId = payload.get("groupId");
        
        authService.authCheck(userId, groupId, token);
        
        try {
            User user = userService.findById(Long.parseLong(userId));
            webSocketService.removeUserFromGroupByUserId(groupId, userId);
            return String.format("User with ID %s left group %s", userId, groupId);
        } catch (Exception e) {
            return String.format("Error handling group leave: %s", e.getMessage());
        }
    }

    @MessageMapping("/group.message")
    public String handleGroupMessage(@Payload Map<String, Object> payload,
                                   @Header("Authorization") String token) {
        String senderId = payload.get("senderId").toString();
        String groupId = payload.get("groupId").toString();

        authService.authCheck(senderId, groupId, token);

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setTimestamp(Instant.now());
        message.setGroupId(groupId);
        message.setContent(payload.get("content").toString());

        try {
            String senderName = userService.findById(Long.parseLong(senderId)).getUsername();
            message.setSenderName(senderName);
            
            webSocketService.sendMessageToGroup(groupId, Map.of(
                "type", "CHAT",
                "senderId", message.getSenderId(),
                "senderName", senderName,
                "content", message.getContent(),
                "timestamp", message.getTimestamp().toString()
            ));

            return String.format("Message sent to topic /topic/group.%s", groupId);
        } catch (Exception e) {
            return String.format("Error sending message: %s", e.getMessage());
        }
    }

    @MessageMapping("group.sync")
    public String handleGroupSync(@Payload Map<String, Object> payload, @Header("Authorization") String token) {
        
        String senderId = payload.get("senderId").toString();
        String groupId = payload.get("groupId").toString();

        authService.authCheck(senderId, groupId, token);

        try{
            User user = userService.findById(Long.parseLong(senderId));
            String senderName = user.getUsername();
            String startTime = payload.get("startTime").toString();
            String duration = payload.get("duration").toString();
            String secondDuration = payload.get("secondDuration").toString();
            String status = user.getStatus().toString();
            String originalDuration = payload.get("originalDuration").toString();


            webSocketService.sendMessageToGroup(groupId, Map.of(
                "type", "SYNC",
                "senderId", senderId,
                "senderName", senderName,
                "startTime", startTime,
                "duration", duration,
                "secondDuration", secondDuration,
                "status", status,
                    "originalDuration", originalDuration
            ));
            return String.format("Message sent to topic /topic/group.%s", groupId);
        } catch (Exception e) {
            System.err.println("Error sending message to: " + e.getMessage());
            return String.format("Error sending sync: %s", e.getMessage());
        }
    }
} 