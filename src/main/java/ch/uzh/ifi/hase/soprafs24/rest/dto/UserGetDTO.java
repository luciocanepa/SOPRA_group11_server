package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserGetDTO {

  private Long id;
  private String token;
  private String username;
  private UserStatus status;
  private List<Long> groupIds;

}
