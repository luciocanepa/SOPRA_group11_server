package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import java.util.List;

public class GroupGetDTO {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Long adminId;
    private List<UserGetDTO> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public List<UserGetDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserGetDTO> users) {
        this.users = users;
    }
}
