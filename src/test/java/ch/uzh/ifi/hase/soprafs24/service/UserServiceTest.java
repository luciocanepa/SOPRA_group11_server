package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserTimerPutDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Duration;
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
  @Mock
  private WebSocketService webSocketService;

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

    // Initialize test user
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testUser");
    testUser.setPassword("password");
    testUser.setToken("test-token");
    testUser.setStatus(UserStatus.ONLINE);

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
    
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    
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
      userWithNoGroups.setToken("test-token");  // Set a token
      userWithNoGroups.setMemberships(Collections.emptyList()); // No groups

      // Mock token validation
      Mockito.when(userRepository.existsByToken("test-token")).thenReturn(true);
      Mockito.when(userRepository.findByToken("test-token")).thenReturn(userWithNoGroups);
      Mockito.when(userRepository.findById(userWithNoGroups.getId())).thenReturn(Optional.of(userWithNoGroups));
      Mockito.when(membershipService.getActiveGroupsForUser(userWithNoGroups)).thenReturn(Collections.emptyList());

      // when
      List<Group> groups = userService.getGroupsForUser(userWithNoGroups.getId(), "test-token");

      // then
      assertNotNull(groups);
      assertTrue(groups.isEmpty(), "Expected an empty list of groups");
  }

  @Test
  void getGroupsForUser_shouldThrowUnauthorized_whenInvalidToken() {
      // given
      String invalidToken = "invalid-token";
      Mockito.when(userRepository.existsByToken(invalidToken)).thenReturn(false);

      // when / then
      assertThrows(ResponseStatusException.class, () -> userService.getGroupsForUser(1L, invalidToken));
  }

  @Test
  void getGroupsForUser_shouldThrowForbidden_whenUserIdDoesNotMatchToken() {
      // given
      Long differentUserId = 2L;
      String validToken = "valid-token";
      
      // Mock that the token exists and returns our testUser (with ID 1)
      Mockito.when(userRepository.existsByToken(validToken)).thenReturn(true);
      Mockito.when(userRepository.findByToken(validToken)).thenReturn(testUser);

      // when / then
      assertThrows(ResponseStatusException.class, () -> userService.getGroupsForUser(differentUserId, validToken));
  }

  @Test
    void updateStatus_validInput_success() {
        // given
        UserTimerPutDTO timerDTO = new UserTimerPutDTO();
        timerDTO.setStartTime(LocalDateTime.now());
        timerDTO.setDuration(Duration.ofMinutes(25));
        timerDTO.setStatus(UserStatus.WORK);
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existingUser");
        existingUser.setStatus(UserStatus.ONLINE);
        existingUser.setToken("test-token");
        
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(existingUser);
        Mockito.when(membershipService.getActiveGroupsForUser(existingUser)).thenReturn(Arrays.asList(group1));
        
        // Mock the WebSocketService.sendTimerUpdate method
        Mockito.doNothing().when(webSocketService).sendTimerUpdate(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString()
        );
        
        // when
        User updatedUser = userService.updateStatus(timerDTO, 1L, "test-token");
        
        // then
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(userRepository, Mockito.times(1)).flush();
        Mockito.verify(webSocketService, Mockito.times(1)).sendTimerUpdate(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString()
        );
        
        assertEquals(timerDTO.getStartTime(), updatedUser.getStartTime());
        assertEquals(timerDTO.getDuration(), updatedUser.getDuration());
        assertEquals(timerDTO.getStatus(), updatedUser.getStatus());
    }

    @Test
    void updateStatus_userNotFound_throwsException() {
        // given
        UserTimerPutDTO timerDTO = new UserTimerPutDTO();
        Long nonExistentUserId = 999L;
        
        Mockito.when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        
        // when/then
        assertThrows(ResponseStatusException.class, () -> userService.updateStatus(timerDTO, nonExistentUserId, "test-token"));
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(userRepository, Mockito.never()).flush();
    }

  @Test
  void logoutUser_shouldThrowUnauthorized_whenInvalidToken() {
      // given
      String invalidToken = "invalid-token";
      Mockito.when(userRepository.existsByToken(invalidToken)).thenReturn(false);

      // when / then
      assertThrows(ResponseStatusException.class, () -> userService.logoutUser(1L, invalidToken));
  }

  @Test
  void logoutUser_shouldThrowForbidden_whenUserIdDoesNotMatchToken() {
      // given
      Long differentUserId = 2L;
      String validToken = "valid-token";
      
      // Mock that the token exists and returns our testUser (with ID 1)
      Mockito.when(userRepository.existsByToken(validToken)).thenReturn(true);
      Mockito.when(userRepository.findByToken(validToken)).thenReturn(testUser);

      // when / then
      assertThrows(ResponseStatusException.class, () -> userService.logoutUser(differentUserId, validToken));
  }

  @Test
  void logoutUser_success() {
      // given
      String validToken = "valid-token";
      Long userId = 1L;
      
      // Mock that the token exists and returns our testUser
      Mockito.when(userRepository.existsByToken(validToken)).thenReturn(true);
      Mockito.when(userRepository.findByToken(validToken)).thenReturn(testUser);
      
      // when
      User loggedOutUser = userService.logoutUser(userId, validToken);
      
      // then
      assertEquals(UserStatus.OFFLINE, loggedOutUser.getStatus());
      Mockito.verify(userRepository).save(testUser);
      Mockito.verify(userRepository).flush();
      
      // Verify WebSocket notifications were sent for each group
      Mockito.verify(webSocketService, Mockito.times(2)).sendTimerUpdate(
          Mockito.eq(testUser.getId().toString()),
          Mockito.eq(testUser.getUsername()),
          Mockito.anyString(),
          Mockito.eq(UserStatus.OFFLINE.toString()),
          Mockito.anyString(),
          Mockito.anyString()
      );
  }
}
