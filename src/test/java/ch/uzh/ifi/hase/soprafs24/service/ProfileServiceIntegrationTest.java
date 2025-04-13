package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
public class ProfileServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setName("testName");
        testUser.setPassword("testPassword");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));
        testUser.setTimezone("UTC");
        testUser.setProfilePicture("default.jpg");
        testUser.setStatus(UserStatus.OFFLINE);
        testUser.setToken("validToken");

        testUser = userRepository.save(testUser);
        userRepository.flush();
    }

    @Test
    public void updateUser_validInputs_success() {
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newUsername");
        userPutDTO.setName("newName");
        userPutDTO.setPassword("newPassword");
        userPutDTO.setBirthday(LocalDate.of(1999, 1, 1));
        userPutDTO.setTimezone("CET");
        userPutDTO.setProfilePicture("new.jpg");

        User updatedUser = userService.putUserEdits(testUser.getId(), userPutDTO, testUser.getToken());

        assertNotNull(updatedUser.getId());
        assertEquals(userPutDTO.getUsername(), updatedUser.getUsername());
        assertEquals(userPutDTO.getName(), updatedUser.getName());
        assertNotEquals("newPassword", updatedUser.getPassword());
        assertEquals(userPutDTO.getBirthday(), updatedUser.getBirthday());
        assertEquals(userPutDTO.getTimezone(), updatedUser.getTimezone());
        assertEquals(userPutDTO.getProfilePicture(), updatedUser.getProfilePicture());

        User dbUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(userPutDTO.getUsername(), dbUser.getUsername());
    }

    @Test
    public void updateUser_invalidToken_throwsException() {
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newUsername");

        assertThrows(ResponseStatusException.class, () -> {
            userService.putUserEdits(testUser.getId(), userPutDTO, "invalidToken");
        });
    }

    @Test
    public void updateUser_duplicateUsername_throwsException() {
        User anotherUser = new User();
        anotherUser.setUsername("anotherUser");
        anotherUser.setToken("anotherToken");
        anotherUser.setPassword("password");
        anotherUser.setStatus(UserStatus.OFFLINE);
        userRepository.save(anotherUser);
        userRepository.flush();

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("anotherUser");

        assertThrows(ResponseStatusException.class, () -> {
            userService.putUserEdits(testUser.getId(), userPutDTO, testUser.getToken());
        });
    }

    @Test
    public void getUserProfile_validId_success() {
        User user = userService.getUserById(testUser.getId(), testUser.getToken());

        assertNotNull(user);
        assertEquals(testUser.getId(), user.getId());
        assertEquals(testUser.getUsername(), user.getUsername());
        assertEquals(testUser.getName(), user.getName());
    }

    @Test
    public void getUserProfile_invalidId_throwsException() {
        assertThrows(ResponseStatusException.class, () -> {
            userService.getUserById(999L, testUser.getToken());
        });
    }

    @Test
    public void updateUser_partialFields_success() {
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setName("updatedName");
        userPutDTO.setTimezone("PST");

        User updatedUser = userService.putUserEdits(testUser.getId(), userPutDTO, testUser.getToken());

        assertEquals("updatedName", updatedUser.getName());
        assertEquals("PST", updatedUser.getTimezone());
        assertEquals(testUser.getUsername(), updatedUser.getUsername());
        assertEquals(testUser.getBirthday(), updatedUser.getBirthday());
    }

    @Test
    public void updateUser_emptyFields_retainOldValues() {
        UserPutDTO userPutDTO = new UserPutDTO();

        User updatedUser = userService.putUserEdits(testUser.getId(), userPutDTO, testUser.getToken());

        assertEquals(testUser.getUsername(), updatedUser.getUsername());
        assertEquals(testUser.getName(), updatedUser.getName());
        assertEquals(testUser.getBirthday(), updatedUser.getBirthday());
        assertEquals(testUser.getTimezone(), updatedUser.getTimezone());
    }

    @Test
    public void updateOtherUser_withWrongToken_throwsException() {
        User otherUser = new User();
        otherUser.setUsername("user2");
        otherUser.setToken("token2");
        otherUser.setPassword("pass");
        otherUser.setStatus(UserStatus.OFFLINE);
        userRepository.saveAndFlush(otherUser);

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setName("HackedName");

        assertThrows(ResponseStatusException.class, () -> {
            userService.putUserEdits(testUser.getId(), userPutDTO, "token2");
        });
    }

    @Test
    public void updateUser_passwordIsEncrypted() {
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setPassword("newSecret");

        User updatedUser = userService.putUserEdits(testUser.getId(), userPutDTO, testUser.getToken());

        assertNotEquals("newSecret", updatedUser.getPassword());
        assertNotNull(updatedUser.getPassword());
        assertTrue(updatedUser.getPassword().length() > 10);
    }

    @Test
    public void updateUser_nullToken_throwsException() {
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("oopsie");

        assertThrows(ResponseStatusException.class, () -> {
            userService.putUserEdits(testUser.getId(), userPutDTO, null);
        });
    }
}
