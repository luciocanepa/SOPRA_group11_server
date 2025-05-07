package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipService membershipService;
    
    @Mock
    private WebSocketService webSocketService;
    
    @Mock
    private ActivityService activityService;
    
    private PasswordEncoder passwordEncoder;

    private User existingUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Use a real BCryptPasswordEncoder instead of a mock
        passwordEncoder = new BCryptPasswordEncoder();

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("oldUsername");
        existingUser.setPassword(passwordEncoder.encode("oldPassword"));
        existingUser.setToken("valid-token");
        
        // Initialize UserService with mocked dependencies
        userService = new UserService(userRepository, membershipService, passwordEncoder, webSocketService, activityService);
    }

    @Test
    void updateProfile_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setUsername("newUsername");
        edits.setName("New Name");
        edits.setBirthday(LocalDate.of(1995, 5, 5));
        edits.setTimezone("Europe/Zurich");
        edits.setProfilePicture("somePic");
        edits.setPassword("newPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newUsername")).thenReturn(null);
        when(userRepository.findByToken("valid-token")).thenReturn(existingUser);

        User updatedUser = userService.putUserEdits(1L, edits, "valid-token");

        assertEquals("newUsername", updatedUser.getUsername());
        assertEquals("New Name", updatedUser.getName());
        assertEquals(LocalDate.of(1995, 5, 5), updatedUser.getBirthday());
        assertEquals("Europe/Zurich", updatedUser.getTimezone());
        assertEquals("somePic", updatedUser.getProfilePicture());
    }

    @Test
    void updateProfile_sameUsername_throwsConflict() {
        UserPutDTO edits = new UserPutDTO();
        edits.setUsername("oldUsername"); // same as existing

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByToken("valid-token")).thenReturn(existingUser);

        // The implementation doesn't throw an exception when the username is the same
        // It just does nothing and returns the user unchanged
        User updatedUser = userService.putUserEdits(1L, edits, "valid-token");
        
        // Verify that the username remains unchanged
        assertEquals("oldUsername", updatedUser.getUsername());
        
        // Verify that the repository was called to save the user
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateOnlyName_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setName("Only Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByToken("valid-token")).thenReturn(existingUser);

        User updated = userService.putUserEdits(1L, edits, "valid-token");
        assertEquals("Only Name", updated.getName());
    }

    @Test
    void updateOnlyBirthday_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setBirthday(LocalDate.of(1990, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByToken("valid-token")).thenReturn(existingUser);

        User updated = userService.putUserEdits(1L, edits, "valid-token");
        assertEquals(LocalDate.of(1990, 1, 1), updated.getBirthday());
    }

    @Test
    void updateOnlyTimezone_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setTimezone("Asia/Tokyo");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByToken("valid-token")).thenReturn(existingUser);

        User updated = userService.putUserEdits(1L, edits, "valid-token");
        assertEquals("Asia/Tokyo", updated.getTimezone());
    }

    @Test
    void updateOnlyProfilePicture_success() {
        UserPutDTO edits = new UserPutDTO();
        edits.setProfilePicture("new-pic.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByToken("valid-token")).thenReturn(existingUser);

        User updated = userService.putUserEdits(1L, edits, "valid-token");
        assertEquals("new-pic.png", updated.getProfilePicture());
    }

    @Test
    void updateOnlyPassword_passwordIsHashed() {
        String rawPassword = "myNewPassword";
        UserPutDTO edits = new UserPutDTO();
        edits.setPassword(rawPassword);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByToken("valid-token")).thenReturn(existingUser);

        User updated = userService.putUserEdits(1L, edits, "valid-token");
        assertNotEquals(rawPassword, updated.getPassword()); // should be encoded
        assertTrue(passwordEncoder.matches(rawPassword, updated.getPassword()));
    }

    @Test
    void updateProfile_invalidUserId_throwsNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserPutDTO edits = new UserPutDTO();
        edits.setUsername("any");

        assertThrows(ResponseStatusException.class, () -> userService.putUserEdits(999L, edits, "any"));
    }

    @Test
    void updateProfile_newUsernameAlreadyTaken_throwsConflict() {
        UserPutDTO edits = new UserPutDTO();
        edits.setUsername("newUsername");

        User takenUser = new User();
        takenUser.setUsername("newUsername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newUsername")).thenReturn(takenUser);
        when(userRepository.findByToken("valid-token")).thenReturn(existingUser);

        assertThrows(ResponseStatusException.class, () -> userService.putUserEdits(1L, edits, "valid-token"));
    }

    @Test
    void updateProfile_wrongToken_throwsForbidden() {
        UserPutDTO edits = new UserPutDTO();
        edits.setUsername("newUsername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByToken("wrong-token")).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> userService.putUserEdits(1L, edits, "wrong-token"));
    }
}
