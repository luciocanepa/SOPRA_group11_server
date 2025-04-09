package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import java.time.LocalDate;
import java.util.List;

public class UserGetDTO {

  private Long id;
  private String token;
  private String username;
  private UserStatus status;
  private List<Long> groupIds;
  private String name;
  private LocalDate birthday;
  private String timezone;
  private String profilePicture;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public List<Long> getGroupIds() {
    return groupIds;
  }

  public void setGroupIds(List<Long> groupIds) {
    this.groupIds = groupIds;
  }


  public String getName() {
    return name;
}

  public void setName(String name) {
    this.name = name;
}  

  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getProfilePicture() {
    return profilePicture;
  }

  public void setProfilePicture(String profilePicture) {
    this.profilePicture = profilePicture;
  }



}
