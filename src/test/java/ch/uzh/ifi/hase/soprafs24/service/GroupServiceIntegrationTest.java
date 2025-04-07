package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupMembershipRepository;
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
class GroupServiceIntegrationTest {

    @Qualifier("groupRepository")
    @Autowired
    private GroupRepository groupRepository;

    @Qualifier("userRepository") 
    @Autowired
    private UserRepository userRepository;
    
    @Qualifier("groupMembershipRepository")
    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setup() {
        groupRepository.deleteAll();
        userRepository.deleteAll();
        membershipRepository.deleteAll();
    }

    @Test
    void createGroup_validInputs_success() {
        // given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setToken("testToken");
        testUser = userService.createUser(testUser);

        Group testGroup = new Group();
        testGroup.setName("testGroup");
        testGroup.setAdminId(testUser.getId());
        testGroup.setMemberships(new ArrayList<>());

        // when
        Group createdGroup = groupService.createGroup(testGroup);

        // then
        assertEquals(testGroup.getName(), createdGroup.getName());
        assertEquals(testUser.getId(), createdGroup.getAdminId());
        assertEquals(1, createdGroup.getActiveUsers().size());
        assertTrue(createdGroup.getActiveUsers().contains(testUser));
    }

    @Test
    void addUserToGroup_validInputs_success() {
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
        testGroup.setMemberships(new ArrayList<>());
        
        testGroup = groupService.createGroup(testGroup);

        // when
        Group updatedGroup = groupService.addUserToGroup(testGroup.getId(), member.getId());

        // then
        assertEquals(2, updatedGroup.getActiveUsers().size());
        assertTrue(updatedGroup.getActiveUsers().contains(admin));
        assertTrue(updatedGroup.getActiveUsers().contains(member));
    }

    @Test
    void createGroup_invalidAdminId_throwsException() {
        // given
        Group testGroup = new Group();
        testGroup.setName("testGroup");
        testGroup.setAdminId(1L);

        // then
        assertThrows(ResponseStatusException.class, () -> groupService.createGroup(testGroup));
    }

    @Test
    void addUserToGroup_invalidGroupId_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        final User createdUser = userService.createUser(testUser);

        // then
        Long invalidGroupId = 1L;
        assertThrows(ResponseStatusException.class, () -> groupService.addUserToGroup(invalidGroupId, createdUser.getId()));
    }
}
