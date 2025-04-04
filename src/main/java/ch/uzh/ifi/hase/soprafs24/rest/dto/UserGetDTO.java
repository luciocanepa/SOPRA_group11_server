package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import java.util.List;

public class UserGetDTO {

  private Long id;
  private String token;
  private String username;
  private UserStatus status;
  private List<Long> groupIds;

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
}
