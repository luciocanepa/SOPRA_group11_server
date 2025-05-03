package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityPostDTO {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
} 