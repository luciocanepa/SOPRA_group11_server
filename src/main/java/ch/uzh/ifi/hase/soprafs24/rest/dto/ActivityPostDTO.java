package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityPostDTO {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
} 