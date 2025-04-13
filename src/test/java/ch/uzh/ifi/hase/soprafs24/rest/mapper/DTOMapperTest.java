package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserTimerPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.InvitationGetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
class DTOMapperTest {
  private DTOMapper dtoMapper;
  private Group testGroup;
  private User testUser;
  private User testInviter;
  private GroupMembership testMembership;

  @BeforeEach
  void setup() {
    dtoMapper = DTOMapper.INSTANCE;

    // Create test user
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testUser");
    testUser.setPassword("password");
    testUser.setToken("token");

    // Create test inviter
    testInviter = new User();
    testInviter.setId(2L);
    testInviter.setUsername("testInviter");
    testInviter.setPassword("password");
    testInviter.setToken("inviter-token");

    // Create test group
    testGroup = new Group();
    testGroup.setId(1L);
    testGroup.setName("testGroup");
    testGroup.setDescription("test description");
    testGroup.setAdminId(testInviter.getId());

    // Create test membership
    testMembership = new GroupMembership();
    testMembership.setId(1L);
    testMembership.setUser(testUser);
    testMembership.setGroup(testGroup);
    testMembership.setStatus(MembershipStatus.PENDING);
    testMembership.setInvitedBy(testInviter.getId());
    testMembership.setInvitedAt(LocalDateTime.now());
  }

  @Test
  void testCreateUser_fromUserPostDTO_toUser_success() {
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
  void testGetUser_fromUser_toUserGetDTO_success() {
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
  void testPutUserTimer_fromUser_toUserTimerPutDTO_success() {
    // create User
    User user = new User();
    user.setStartTime(LocalDateTime.parse("2023-11-15T14:30:45"));
    user.setDuration(Duration.parse("PT25M"));
    user.setStatus(UserStatus.WORK);

    // MAP -> Create UserTimerPutDTO
    UserTimerPutDTO userTimerPutDTO = DTOMapper.INSTANCE.convertEntityToUserTimerPutDTO(user);

    // check content
    assertEquals(user.getStartTime(), userTimerPutDTO.getStartTime());
    assertEquals(user.getDuration(), userTimerPutDTO.getDuration());
    assertEquals(user.getStatus(), userTimerPutDTO.getStatus());
  }

  @Test
  void testCreateGroup_fromGroupPostDTO_toGroup_success() {
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
  void testGetGroup_fromGroup_toGroupGetDTO_success() {
    // create Group with Users
    Group group = new Group();
    group.setId(1L);
    group.setName("testGroup");
    group.setDescription("test description");
    group.setImage("test.jpg");
    group.setAdminId(1L);
    group.setMemberships(new ArrayList<>());

    User user = new User();
    user.setId(1L);
    user.setUsername("testUser");
    user.setStatus(UserStatus.ONLINE);
    user.setMemberships(new ArrayList<>());

    GroupMembership membership = new GroupMembership();
    membership.setUser(user);
    membership.setGroup(group);
    membership.setStatus(MembershipStatus.ACTIVE);
    group.getMemberships().add(membership);
    user.getMemberships().add(membership);

    // MAP -> Create GroupGetDTO
    GroupGetDTO groupGetDTO = DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group);

    // check content
    assertEquals(group.getId(), groupGetDTO.getId());
    assertEquals(group.getName(), groupGetDTO.getName());
    assertEquals(group.getDescription(), groupGetDTO.getDescription());
    assertEquals(group.getImage(), groupGetDTO.getImage());
    assertEquals(group.getAdminId(), groupGetDTO.getAdminId());
    assertEquals(group.getActiveUsers().get(0).getId(), groupGetDTO.getUsers().get(0).getId());
    assertEquals(group.getActiveUsers().get(0).getUsername(), groupGetDTO.getUsers().get(0).getUsername());
  }

  @Test
  void convertMembershipToInvitationGetDTO_success() {
    // when
    InvitationGetDTO dto = dtoMapper.convertMembershipToInvitationGetDTO(testMembership);

    // then
    assertNotNull(dto);
    assertEquals(testMembership.getId(), dto.getId());
    assertEquals(testGroup.getId(), dto.getGroupId());
    assertEquals(testGroup.getName(), dto.getGroupName());
    assertEquals(testInviter.getId(), dto.getInviterId());
    assertEquals(testUser.getId(), dto.getInviteeId());
    assertEquals(MembershipStatus.PENDING, dto.getStatus());
    assertEquals(testMembership.getInvitedAt(), dto.getInvitedAt());
  }

  @Test
  void convertActiveUsers_success() {
    // given
    GroupMembership activeMembership = new GroupMembership();
    activeMembership.setUser(testUser);
    activeMembership.setStatus(MembershipStatus.ACTIVE);
    testGroup.setMemberships(List.of(activeMembership));

    // when
    List<UserGetDTO> users = dtoMapper.convertActiveUsers(testGroup);

    // then
    assertNotNull(users);
    assertEquals(1, users.size());
    assertEquals(testUser.getId(), users.get(0).getId());
    assertEquals(testUser.getUsername(), users.get(0).getUsername());
  }

  @Test
  void convertActiveUsers_emptyList() {
    // given
    testGroup.setMemberships(List.of());

    // when
    List<UserGetDTO> users = dtoMapper.convertActiveUsers(testGroup);

    // then
    assertNotNull(users);
    assertTrue(users.isEmpty());
  }

  @Test
  void convertActiveUsers_nullMemberships() {
    // given
    testGroup.setMemberships(null);

    // when
    List<UserGetDTO> users = dtoMapper.convertActiveUsers(testGroup);

    // then
    assertNotNull(users);
    assertTrue(users.isEmpty());
  }

  @Test
  void convertActiveGroupsToIds_success() {
    // given
    GroupMembership activeMembership = new GroupMembership();
    activeMembership.setGroup(testGroup);
    activeMembership.setStatus(MembershipStatus.ACTIVE);
    testUser.setMemberships(List.of(activeMembership));

    // when
    List<Long> groupIds = dtoMapper.convertActiveGroupsToIds(testUser);

    // then
    assertNotNull(groupIds);
    assertEquals(1, groupIds.size());
    assertEquals(testGroup.getId(), groupIds.get(0));
  }

  @Test
  void convertActiveGroupsToIds_emptyList() {
    // given
    testUser.setMemberships(List.of());

    // when
    List<Long> groupIds = dtoMapper.convertActiveGroupsToIds(testUser);

    // then
    assertNotNull(groupIds);
    assertTrue(groupIds.isEmpty());
  }

  @Test
  void convertActiveGroupsToIds_nullMemberships() {
    // given
    testUser.setMemberships(null);

    // when
    List<Long> groupIds = dtoMapper.convertActiveGroupsToIds(testUser);

    // then
    assertNotNull(groupIds);
    assertTrue(groupIds.isEmpty());
  }
}
