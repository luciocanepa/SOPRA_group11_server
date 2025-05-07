package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Represents a timer update message sent through WebSocket
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TimerUpdate implements Serializable {
    private String userId;
    private String username; 
    private String status;
    private long duration;
    private String startTime;
}