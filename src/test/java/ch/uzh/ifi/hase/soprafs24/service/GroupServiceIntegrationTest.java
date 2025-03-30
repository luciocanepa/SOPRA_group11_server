package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
public class GroupServiceIntegrationTest {

    @Qualifier("groupRepository")
    @Autowired
    private GroupRepository groupRepository;

    @Qualifier("userRepository") 
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void createGroup_validInputs_success() {
        // given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setToken("testToken");
        testUser.setId(1L);
        testUser = userService.createUser(testUser);

        Group testGroup = new Group();
        testGroup.setName("testGroup");
        testGroup.setAdminId(testUser.getId());
        testGroup.setUsers(new ArrayList<>());
        testGroup.getUsers().add(testUser);
        
        testUser.getGroups().add(testGroup);

        // when
        Group createdGroup = groupService.createGroup(testGroup);

        // then
        assertEquals(testGroup.getName(), createdGroup.getName());
        assertEquals(testUser.getId(), createdGroup.getAdminId());
        assertEquals(1, createdGroup.getUsers().size());
        assertTrue(createdGroup.getUsers().contains(testUser));
    }

    @Test
    public void addUserToGroup_validInputs_success() {
        // given
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("password");
        admin.setStatus(UserStatus.ONLINE);
        admin.setToken("adminToken");
        admin = userService.createUser(admin);

        User member = new User();
        member.setUsername("member");
        member.setPassword("password");
        member.setStatus(UserStatus.ONLINE);
        member.setToken("memberToken");
        member = userService.createUser(member);

        Group testGroup = new Group();
        testGroup.setName("testGroup");
        testGroup.setAdminId(admin.getId());
        testGroup.setUsers(new ArrayList<>());
        testGroup.getUsers().add(admin);
        admin.getGroups().add(testGroup);
        
        testGroup = groupService.createGroup(testGroup);

        // when
        Group updatedGroup = groupService.addUserToGroup(testGroup.getId(), member.getId());

        // then
        assertEquals(2, updatedGroup.getUsers().size());
        assertTrue(updatedGroup.getUsers().contains(admin));
        assertTrue(updatedGroup.getUsers().contains(member));
    }

    @Test
    public void createGroup_invalidAdminId_throwsException() {
        // given
        Group testGroup = new Group();
        testGroup.setName("testGroup");
        testGroup.setAdminId(1L);

        // then
        assertThrows(ResponseStatusException.class, () -> groupService.createGroup(testGroup));
    }

    @Test
    public void addUserToGroup_invalidGroupId_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        final User createdUser = userService.createUser(testUser);

        // then
        assertThrows(ResponseStatusException.class, () -> groupService.addUserToGroup(1L, createdUser.getId()));
    }
}
