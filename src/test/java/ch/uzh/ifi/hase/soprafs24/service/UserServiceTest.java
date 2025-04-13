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
  @Mock
  private MembershipService membershipService;

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
    testUser.setToken("valid-token");

    group1 = new Group();
    group1.setId(1L);
    group1.setName("Group 1");

    group2 = new Group();
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
    Mockito.when(userRepository.existsByToken("valid-token")).thenReturn(true);
    Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
    
    // Mock the membershipService to return the groups
    Mockito.when(membershipService.getActiveGroupsForUser(Mockito.any(User.class)))
        .thenReturn(Arrays.asList(group1, group2));
    
    // Mock password encoder behavior
    Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");
    Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
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
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
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
    testUser.setToken("test-token");
    
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
    Mockito.when(passwordEncoder.matches(loginUser.getPassword(), testUser.getPassword())).thenReturn(true);
    Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(testUser);
    
    // when
    User loggedInUser = userService.loginUser(loginUser);
    
    // then
    Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.any());
    Mockito.verify(passwordEncoder, Mockito.times(1)).matches(loginUser.getPassword(), testUser.getPassword());
    
    Mockito.verify(userRepository, Mockito.times(2)).save(Mockito.any(User.class));
    
    assertEquals(testUser.getId(), loggedInUser.getId());
    assertEquals(testUser.getUsername(), loggedInUser.getUsername());
    assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
    assertEquals("test-token", loggedInUser.getToken());
  }

  @Test
  void loginUser_userNotFound_throwsException() {
    // given
    User loginUser = new User();
    loginUser.setUsername("nonExistingUser");
    loginUser.setPassword("testPassword");
    
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
    
    testUser.setPassword("encodedPassword");
    
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
    Mockito.when(passwordEncoder.matches(loginUser.getPassword(), testUser.getPassword())).thenReturn(false);
    
    // when/then
    assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginUser));
  }

  @Test
  void getGroupsForUser_shouldReturnActiveGroups_whenUserExists() {
      // when
      List<Group> groups = userService.getGroupsForUser(testUser.getId(), "valid-token");

      // then
      assertNotNull(groups);
      assertEquals(2, groups.size()); // we expect 2 groups
      assertEquals("Group 1", groups.get(0).getName());
      assertEquals("Group 2", groups.get(1).getName());
      
      // Verify that membershipService.getActiveGroupsForUser was called
      Mockito.verify(membershipService, Mockito.times(1)).getActiveGroupsForUser(Mockito.any(User.class));
  }

  @Test
  void getGroupsForUser_shouldThrowNotFoundException_whenUserDoesNotExist() {
      // given
      Long nonExistentUserId = 999L;
      Mockito.when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

      // when / then
      assertThrows(ResponseStatusException.class, () -> userService.getGroupsForUser(nonExistentUserId, "valid-token"));
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
      Mockito.when(membershipService.getActiveGroupsForUser(userWithNoGroups)).thenReturn(Collections.emptyList());

      // when
      List<Group> groups = userService.getGroupsForUser(userWithNoGroups.getId(), "valid-token");

      // then
      assertNotNull(groups);
      assertTrue(groups.isEmpty(), "Expected an empty list of groups");
  }
}
