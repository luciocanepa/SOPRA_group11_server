package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipService membershipService;

    private User existingUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("oldUsername");
        existingUser.setPassword(new BCryptPasswordEncoder().encode("oldPassword"));
        existingUser.setToken("valid-token");
    }

    @Test
    void updateProfile_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setToken("valid-token");
        edits.setUsername("newUsername");
        edits.setName("New Name");
        edits.setBirthday(LocalDate.of(1995, 5, 5));
        edits.setTimezone("Europe/Zurich");
        edits.setProfilePicture("somePic");
        edits.setPassword("newPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newUsername")).thenReturn(null);

        User updatedUser = userService.putUserEdits(1L, edits);

        assertEquals("newUsername", updatedUser.getUsername());
        assertEquals("New Name", updatedUser.getName());
        assertEquals(LocalDate.of(1995, 5, 5), updatedUser.getBirthday());
        assertEquals("Europe/Zurich", updatedUser.getTimezone());
        assertEquals("somePic", updatedUser.getProfilePicture());
    }

    @Test
    void updateProfile_sameUsername_throwsConflict() {
        UserPutDTO edits = new UserPutDTO();
        edits.setToken("valid-token");
        edits.setUsername("oldUsername"); // same as existing

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.putUserEdits(1L, edits));
        assertEquals("409 CONFLICT", ex.getStatus().toString());
    }

    @Test
    void updateOnlyName_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setToken("valid-token");
        edits.setName("Only Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        User updated = userService.putUserEdits(1L, edits);
        assertEquals("Only Name", updated.getName());
    }

    @Test
    void updateOnlyBirthday_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setToken("valid-token");
        edits.setBirthday(LocalDate.of(1990, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        User updated = userService.putUserEdits(1L, edits);
        assertEquals(LocalDate.of(1990, 1, 1), updated.getBirthday());
    }

    @Test
    void updateOnlyTimezone_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setToken("valid-token");
        edits.setTimezone("Asia/Tokyo");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        User updated = userService.putUserEdits(1L, edits);
        assertEquals("Asia/Tokyo", updated.getTimezone());
    }

    @Test
    void updateOnlyProfilePicture_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setToken("valid-token");
        edits.setProfilePicture("new-pic.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        User updated = userService.putUserEdits(1L, edits);
        assertEquals("new-pic.png", updated.getProfilePicture());
    }

    @Test
    void updateOnlyPassword_passwordIsHashed() {
        String rawPassword = "myNewPassword";
        UserPutDTO edits = new UserPutDTO();
        edits.setToken("valid-token");
        edits.setPassword(rawPassword);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        User updated = userService.putUserEdits(1L, edits);
        assertNotEquals(rawPassword, updated.getPassword()); // should be encoded
        assertTrue(new BCryptPasswordEncoder().matches(rawPassword, updated.getPassword()));
    }

    @Test
    void updateProfile_invalidUserId_throwsNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserPutDTO edits = new UserPutDTO();
        edits.setToken("any");

        assertThrows(ResponseStatusException.class, () -> userService.putUserEdits(999L, edits));
    }

    @Test
    void updateProfile_newUsernameAlreadyTaken_throwsConflict() {
        UserPutDTO edits = new UserPutDTO();
        edits.setToken("valid-token");
        edits.setUsername("newUsername");

        User takenUser = new User();
        takenUser.setUsername("newUsername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newUsername")).thenReturn(takenUser);

        assertThrows(ResponseStatusException.class, () -> userService.putUserEdits(1L, edits));
    }

    @Test
    void updateProfile_wrongToken_throwsForbidden() {
        UserPutDTO edits = new UserPutDTO();
        edits.setToken("wrong-token");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        assertThrows(ResponseStatusException.class, () -> userService.putUserEdits(1L, edits));
    }
}
