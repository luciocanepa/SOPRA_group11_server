package ch.uzh.ifi.hase.soprafs24.entity;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private String senderId;
    private String senderName;
    private String groupId;
    private String content;
    private Instant timestamp;
}
