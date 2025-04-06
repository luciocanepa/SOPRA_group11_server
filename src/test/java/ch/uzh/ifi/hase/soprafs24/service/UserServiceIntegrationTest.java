package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

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
public class UserServiceIntegrationTest {

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Qualifier("groupRepository")
  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll();
    groupRepository.deleteAll();
  }

  @Test
  public void createUser_validInputs_success() {
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
  public void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setStatus(UserStatus.ONLINE);
    User createdUser = userService.createUser(testUser);

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
    public void getGroupsForUser_shouldReturnActiveGroups_whenUserExists() {
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

        testUser.addMembership(membership1);
        testUser.addMembership(membership2);
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
    public void getGroupsForUser_shouldThrowException_whenUserDoesNotExist() {
        // given
        long nonExistentUserId = 999L;

        // when & then
        assertThrows(ResponseStatusException.class, () -> userService.getGroupsForUser(nonExistentUserId));
    }

    // edge case: user is part of no groups
    @Test
    public void getGroupsForUser_shouldReturnEmptyList_whenUserHasNoGroups() {
        // Given: A user with no active groups in the database
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setMemberships(Collections.emptyList());

        userService.createUser(testUser);

        userRepository.save(testUser); // Save to the actual DB in an integration test

        // When
        List<Group> groups = userService.getGroupsForUser(1L);

        // Then
        assertTrue(groups.isEmpty(), "Expected an empty list of groups");
    }

}
