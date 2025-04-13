package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserTimerPutDTO;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Qualifier("groupRepository")
  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  void setup() {
    userRepository.deleteAll();
    groupRepository.deleteAll();
  }

  @Test
  void createUser_validInputs_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setStatus(UserStatus.ONLINE);

    // when
    User createdUser = userService.createUser(testUser);

    // then
    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getPassword());
    assertNotNull(createdUser.getToken());
    assertNotNull(createdUser.getId());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setStatus(UserStatus.ONLINE);
    userService.createUser(testUser);

    // attempt to create second user with same username
    User testUser2 = new User();

    // create second user with same username
    testUser2.setUsername("testUsername");
    testUser2.setPassword("testPassword2");
    testUser2.setStatus(UserStatus.ONLINE);

    // check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
  }

  @Test
  void getGroupsForUser_shouldReturnActiveGroups_whenUserExists() {
      // given
      User testUser = new User();
      testUser.setUsername("testUsername");
      testUser.setPassword("testPassword");
      testUser.setStatus(UserStatus.ONLINE);
      userService.createUser(testUser);

      // Create groups
      Group group1 = new Group();
      group1.setName("Group 1");
      group1.setAdminId(testUser.getId());
      groupRepository.save(group1);

      Group group2 = new Group();
      group2.setName("Group 2");
      group2.setAdminId(testUser.getId());
      groupRepository.save(group2);

      // Add memberships
      GroupMembership membership1 = new GroupMembership();
      membership1.setGroup(group1);
      membership1.setUser(testUser);
      membership1.setStatus(MembershipStatus.ACTIVE);

      GroupMembership membership2 = new GroupMembership();
      membership2.setGroup(group2);
      membership2.setUser(testUser);
      membership2.setStatus(MembershipStatus.ACTIVE);

      testUser.getMemberships().add(membership1);
      testUser.getMemberships().add(membership2);
      userRepository.save(testUser);

      // when
      var activeGroups = userService.getGroupsForUser(testUser.getId());

      // then
      assertNotNull(activeGroups);
      assertEquals(2, activeGroups.size());
      assertTrue(activeGroups.contains(group1));
      assertTrue(activeGroups.contains(group2));
    }

    @Test
    void getGroupsForUser_shouldThrowException_whenUserDoesNotExist() {
        // given
        long nonExistentUserId = 999L;

        // when & then
        assertThrows(ResponseStatusException.class, () -> userService.getGroupsForUser(nonExistentUserId));
    }

    // edge case: user is part of no groups
    @Test
    void getGroupsForUser_shouldReturnEmptyList_whenUserHasNoGroups() {
        // Given: A user with no active groups in the database
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setMemberships(Collections.emptyList());

        // Create the user and get the generated ID
        User createdUser = userService.createUser(testUser);
        Long userId = createdUser.getId();

        // When
        List<Group> groups = userService.getGroupsForUser(userId);

        // Then
        assertTrue(groups.isEmpty(), "Expected an empty list of groups");
    }
  
    @Test
    void loginUser_validInput_success() {

      User testUser = new User();
      testUser.setUsername("testUsername");
      testUser.setPassword("testPassword");
      userService.createUser(testUser);

      User testUser2 = new User();
      testUser2.setUsername("testUsername");
      testUser2.setPassword("testPassword");

      //when
      User loginUser = userService.loginUser(testUser2);

      //then
      assertEquals(testUser.getId(), loginUser.getId());
      assertEquals(testUser.getUsername(), loginUser.getUsername());
      assertNotNull(loginUser.getToken());
      assertNotNull(loginUser.getId());
      assertNotNull(loginUser.getPassword());
      assertEquals(UserStatus.ONLINE, loginUser.getStatus());

    }

    @Test
    void loginUser_missingUser_throwsException() {

      User testUser = new User();
      testUser.setUsername("testUsername");
      testUser.setPassword("testPassword");

      //check that an error is thrown
      assertThrows(ResponseStatusException.class, () -> userService.loginUser(testUser));

    }

    @Test
    void loginUser_wrongPassword_throwsException() {

      User testUser = new User();
      testUser.setUsername("testUsername");
      testUser.setPassword("testPassword");
      userService.createUser(testUser);

      //login user with wrong password
      User testUser2 = new User();
      testUser2.setUsername("testUsername");
      testUser2.setPassword("wrongPasword");

      //check that error is thrown
      assertThrows(ResponseStatusException.class, () -> userService.loginUser(testUser2));

    }

    @Test
    void updateUserTimer_validInput_success() {

      User testUser = new User();
      testUser.setUsername("testUsername");
      testUser.setPassword("testPassword");
      testUser = userService.createUser(testUser);

      UserTimerPutDTO testDTO = new UserTimerPutDTO();
      testDTO.setStartTime(LocalDateTime.parse("2023-11-15T14:30:45"));
      testDTO.setDuration(Duration.parse("PT25M"));
      testDTO.setStatus(UserStatus.WORK);

      User updatedUser = userService.updateStatus(testDTO, testUser.getId());

      assertEquals(testDTO.getStartTime(), updatedUser.getStartTime());
      assertEquals(testDTO.getDuration(), updatedUser.getDuration());
      assertEquals(testDTO.getStatus(), updatedUser.getStatus());
    }

    @Test
    void updateUserTimer_missingUser_throwsException() {

      UserTimerPutDTO testDTO = new UserTimerPutDTO();
      testDTO.setStartTime(LocalDateTime.parse("2023-11-15T14:30:45"));
      testDTO.setDuration(Duration.parse("PT25M"));
      testDTO.setStatus(UserStatus.WORK);

      assertThrows(ResponseStatusException.class, () -> userService.updateStatus(testDTO,1L));

    }
}
