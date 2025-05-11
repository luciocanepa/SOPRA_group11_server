package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PlannedStudySessions")
@Getter
@Setter
public class CalendarEntries {
    @Id
    @GeneratedValue
    private Long id;

    private Long groupId;
    private String createdByUsername;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String title;
    private String description;
}
