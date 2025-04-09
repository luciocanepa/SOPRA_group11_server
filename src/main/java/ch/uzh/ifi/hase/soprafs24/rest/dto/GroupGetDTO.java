package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupGetDTO {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Long adminId;
    private List<UserGetDTO> users;

}
