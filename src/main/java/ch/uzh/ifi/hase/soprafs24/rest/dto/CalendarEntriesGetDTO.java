package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarEntriesGetDTO {
    private Long id;
    private Long groupId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String createdByUsername;
}
