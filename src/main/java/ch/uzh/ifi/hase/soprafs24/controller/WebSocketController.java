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
import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.TimerUpdate;

import java.util.Map;
import java.time.LocalDateTime;
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
     * Handles a user joining a group
     * This method is called when a user is added to a group in the database
     * 
     * @param payload Contains userId and groupId
     */
    @MessageMapping("/group.join")
    public String handleGroupJoin(@Payload Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String userId = payload.get("userId");
        String groupId = payload.get("groupId");
        String sessionId = headerAccessor.getSessionId();
        
        try {
            User user = userService.findById(Long.parseLong(userId));            
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
    public String handleGroupLeave(@Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        String groupId = payload.get("groupId");
        
        try {
            User user = userService.findById(Long.parseLong(userId));
            webSocketService.removeUserFromGroupByUserId(groupId, userId);

            return String.format("User with ID %s left group %d", userId, groupId);
        } catch (Exception e) {
            return String.format("Error handling group leave: %s", e.getMessage());
        }
    }

    @MessageMapping("/group.message")
    public String handleGroupMessage(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String senderId = payload.get("senderId").toString();
        String groupId = payload.get("groupId").toString();

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setTimestamp(LocalDateTime.now());
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

    // /**
    //  * Handles subscription to a group topic
    //  * This method is called when a client subscribes to a group topic
    //  * 
    //  * @return A welcome message
    //  */
    // @SubscribeMapping("/topic/group/{groupId}")
    // public String handleGroupSubscription() {
    //     return "Welcome to the group!";
    // }
} 