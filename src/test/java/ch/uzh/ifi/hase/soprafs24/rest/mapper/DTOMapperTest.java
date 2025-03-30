package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupPostDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
  @Test
  public void testCreateUser_fromUserPostDTO_toUser_success() {
    // create UserPostDTO
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("username");
    userPostDTO.setPassword("password");

    // MAP -> Create user
    User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // check content
    assertEquals(userPostDTO.getUsername(), user.getUsername());
    assertEquals(userPostDTO.getPassword(), user.getPassword());
  }

  @Test
  public void testGetUser_fromUser_toUserGetDTO_success() {
    // create User
    User user = new User();
    user.setUsername("firstname@lastname");
    user.setPassword("password");
    user.setId(1L);
    user.setStatus(UserStatus.OFFLINE);
    user.setToken("1");

    // MAP -> Create UserGetDTO
    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

    // check content
    assertEquals(user.getId(), userGetDTO.getId());
    assertEquals(user.getUsername(), userGetDTO.getUsername());
    assertEquals(user.getStatus(), userGetDTO.getStatus());
  }

  @Test
  public void testCreateGroup_fromGroupPostDTO_toGroup_success() {
    // create GroupPostDTO
    GroupPostDTO groupPostDTO = new GroupPostDTO();
    groupPostDTO.setName("testGroup");
    groupPostDTO.setDescription("test description");
    groupPostDTO.setImage("test.jpg");
    groupPostDTO.setAdminId(1L);

    // MAP -> Create group
    Group group = DTOMapper.INSTANCE.convertGroupPostDTOtoEntity(groupPostDTO);

    // check content
    assertEquals(groupPostDTO.getName(), group.getName());
    assertEquals(groupPostDTO.getDescription(), group.getDescription());
    assertEquals(groupPostDTO.getImage(), group.getImage());
    assertEquals(groupPostDTO.getAdminId(), group.getAdminId());
  }

  @Test
  public void testGetGroup_fromGroup_toGroupGetDTO_success() {
    // create Group with Users
    Group group = new Group();
    group.setId(1L);
    group.setName("testGroup");
    group.setDescription("test description");
    group.setImage("test.jpg");
    group.setAdminId(1L);

    User user = new User();
    user.setId(1L);
    user.setUsername("testUser");
    user.setStatus(UserStatus.ONLINE);

    List<User> users = new ArrayList<>();
    users.add(user);
    group.setUsers(users);

    // MAP -> Create GroupGetDTO
    GroupGetDTO groupGetDTO = DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group);

    // check content
    assertEquals(group.getId(), groupGetDTO.getId());
    assertEquals(group.getName(), groupGetDTO.getName());
    assertEquals(group.getDescription(), groupGetDTO.getDescription());
    assertEquals(group.getImage(), groupGetDTO.getImage());
    assertEquals(group.getAdminId(), groupGetDTO.getAdminId());
    assertEquals(group.getUsers().get(0).getId(), groupGetDTO.getUsers().get(0).getId());
    assertEquals(group.getUsers().get(0).getUsername(), groupGetDTO.getUsers().get(0).getUsername());
  }
}
