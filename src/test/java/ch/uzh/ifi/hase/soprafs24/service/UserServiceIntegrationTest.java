package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
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
    assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
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
