package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll();
  }

  @Test
  public void createUser_validInputs_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    // when
    User createdUser = userService.createUser(testUser);

    // then
    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertNotNull(createdUser.getId());
    assertNotNull(createdUser.getPassword());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    userService.createUser(testUser);

    // attempt to create second user with same username
    User testUser2 = new User();

    // change the name but forget about the username
    testUser2.setUsername("testUsername");
    testUser2.setPassword("testPassword2");

    // check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
  }

  @Test
  public void loginUser_validInput_success() {

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
  public void loginUser_missingUser_throwsException() {

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    //check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> userService.loginUser(testUser));

  }

  @Test
  public void loginUser_wrongPassword_throwsException() {

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
}
