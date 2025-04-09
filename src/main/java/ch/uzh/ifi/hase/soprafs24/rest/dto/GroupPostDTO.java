package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupPostDTO {
    private String name;
    private String description;
    private String image;
    private Long adminId;

}
