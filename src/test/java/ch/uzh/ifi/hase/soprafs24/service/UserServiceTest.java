package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
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

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private User testUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
  }

  @Test
  public void createUser_validInputs_success() {
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
  public void createUser_duplicateUsername_throwsException() {
    // given -> a first user has already been created
    userService.createUser(testUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
  }

  @Test
  public void loginUser_validCredentials_success() {
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
  public void loginUser_userNotFound_throwsException() {
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
  public void loginUser_wrongPassword_throwsException() {
    // given
    User loginUser = new User();
    loginUser.setUsername("testUsername");
    loginUser.setPassword("wrongPassword");
    
    // mock repository to return testUser
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
    
    // when/then
    assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginUser));
    
  }

  //test not needed anymore. for safety i wont delete it yet
  /* 
  @Test
  public void createUser_duplicateInputs_throwsException() {
    // given -> a first user has already been created
    userService.createUser(testUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByName(Mockito.any())).thenReturn(testUser);
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
  }
  */
}
