package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private User testUser;
  private Group group1;
  private Group group2;
  private GroupMembership membership1;
  private GroupMembership membership2;


    @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

      Group group1 = new Group();
      group1.setId(1L);
      group1.setName("Group 1");

      Group group2 = new Group();
      group2.setId(2L);
      group2.setName("Group 2");

      membership1 = new GroupMembership();
      membership1.setGroup(group1);
      membership1.setStatus(MembershipStatus.ACTIVE);

      membership2 = new GroupMembership();
      membership2.setGroup(group2);
      membership2.setStatus(MembershipStatus.ACTIVE);

      testUser.setMemberships(Arrays.asList(membership1, membership2));

    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
        Mockito.when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
  }

  @Test
  void createUser_validInputs_success() {
    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    User createdUser = userService.createUser(testUser);

    // then
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getPassword());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
  }

  @Test
  void createUser_duplicateUsername_throwsException() {
    // given -> a first user has already been created
    userService.createUser(testUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
  }

  @Test
  void loginUser_validCredentials_success() {
    // given
    User loginUser = new User();
    loginUser.setUsername("testUsername");
    loginUser.setPassword("testPassword");

    testUser.setPassword("encodedPassword");
    
    // mock repository to return testUser when findByUsername is called

    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
    Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

    
    // when
    User loggedInUser = userService.loginUser(loginUser);
    
    // then
    Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.any());
    
    
    assertEquals(testUser.getId(), loggedInUser.getId());
    assertEquals(testUser.getUsername(), loggedInUser.getUsername());
    assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
    assertNotNull(loggedInUser.getToken());
  }

  @Test
  void loginUser_userNotFound_throwsException() {
    // given
    User loginUser = new User();
    loginUser.setUsername("nonExistingUser");
    loginUser.setPassword("testPassword");
    
    // mock repository to return null when user is not found
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
    
    // when/then
    assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginUser));
    
  }

  @Test
  void loginUser_wrongPassword_throwsException() {
    // given
    User loginUser = new User();
    loginUser.setUsername("testUsername");
    loginUser.setPassword("wrongPassword");
    
    // mock repository to return testUser
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
    
    // when/then
    assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginUser));
    
  }

  @Test
  void getGroupsForUser_shouldReturnActiveGroups_whenUserExists() {
      // when
      List<Group> groups = userService.getGroupsForUser(testUser.getId());

      // then
      assertNotNull(groups);
      assertEquals(2, groups.size()); // we expect 2 groups
      assertEquals("Group 1", groups.get(0).getName());
      assertEquals("Group 2", groups.get(1).getName());
  }

  @Test
  void getGroupsForUser_shouldThrowNotFoundException_whenUserDoesNotExist() {
      // given
      Long nonExistentUserId = 999L;
      Mockito.when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

      // when / then
      assertThrows(ResponseStatusException.class, () -> userService.getGroupsForUser(nonExistentUserId));
  }

  // Test if the method returns an empty list in the edge case if the user is part of no groups
  @Test
  void getGroupsForUser_shouldReturnEmptyList_whenUserHasNoGroups() {
      // given: user with no groups
      User userWithNoGroups = new User();
      userWithNoGroups.setId(2L);
      userWithNoGroups.setUsername("userWithNoGroups");
      userWithNoGroups.setMemberships(Collections.emptyList()); // No groups

      Mockito.when(userRepository.findById(userWithNoGroups.getId())).thenReturn(Optional.of(userWithNoGroups));

      // when
      List<Group> groups = userService.getGroupsForUser(userWithNoGroups.getId());

      // then
      assertNotNull(groups);
      assertTrue(groups.isEmpty(), "Expected an empty list of groups");
  }
}
