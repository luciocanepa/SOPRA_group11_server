import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceIntegrationTest {

    @Test
    public void createUser_duplicateUsername_throwsException() {
        // Create first user
        User user1 = new User();
        user1.setUsername("testuser");
        user1.setPassword("password123");
        userService.createUser(user1);

        // Try to create second user with same username
        User user2 = new User();
        user2.setUsername("testuser");  // Same username as user1
        user2.setPassword("differentpassword");

        // Assert that the expected exception is thrown
        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.createUser(user2);
        });
    }
}