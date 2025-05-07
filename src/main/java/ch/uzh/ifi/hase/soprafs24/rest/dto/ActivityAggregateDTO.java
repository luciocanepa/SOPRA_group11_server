package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityAggregateDTO {
    private LocalDate date;
    private long duration;
}