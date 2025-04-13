package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.Duration;
import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTimerPutDTO {
    private LocalDateTime startTime;
    private Duration duration;
    private UserStatus status;
    
}
