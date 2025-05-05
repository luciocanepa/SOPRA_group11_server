package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserActivitiesGetDTO {
    private Long userId;
    private String username;
    private String name;
    private String profilePicture;
    private List<ActivityGetDTO> activities;
} 