package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
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
import java.time.LocalDateTime;

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

    @Autowired
    private MembershipService membershipService;

    private Group testGroup;
    private User testUser;
    private GroupMembership testMembership;

    @BeforeEach
    void setup() {
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser.setToken("token");
        testUser.setStatus(UserStatus.ONLINE);
        testUser = userRepository.save(testUser);

        // Create test group
        testGroup = new Group();
        testGroup.setName("testGroup");
        testGroup.setAdminId(testUser.getId());
        testGroup = groupRepository.save(testGroup);

        // Create test membership
        testMembership = new GroupMembership();
        testMembership.setUser(testUser);
        testMembership.setGroup(testGroup);
        testMembership.setStatus(MembershipStatus.ACTIVE);
        testMembership.setInvitedBy(testUser.getId());
        testMembership.setInvitedAt(LocalDateTime.now());
        
        // Set up bidirectional relationships
        testGroup.getMemberships().add(testMembership);
        testUser.getMemberships().add(testMembership);
        
        // Save the entities
        testMembership = membershipRepository.save(testMembership);
        testGroup = groupRepository.save(testGroup);
        testUser = userRepository.save(testUser);
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

    @Test
    void deleteGroup_success() {
        // given
        assertNotNull(testGroup.getId());
        assertNotNull(testMembership.getId());
        assertEquals(1, membershipRepository.findByGroup(testGroup).size());
        
        // when
        groupService.deleteGroup(testGroup.getId());
        
        // then
        assertFalse(groupRepository.findById(testGroup.getId()).isPresent());
        assertEquals(0, membershipRepository.findByGroup(testGroup).size());
        assertEquals(0, membershipRepository.findByUser(testUser).size());
    }
    
    @Test
    void updateGroup_validInputs_success() {
        // given
        assertNotNull(testGroup.getId());
        assertEquals("testGroup", testGroup.getName());
        
        // Create a group with updated information
        Group updatedGroup = new Group();
        updatedGroup.setName("Updated Group Name");
        updatedGroup.setDescription("Updated Description");
        updatedGroup.setImage("Updated Image URL");
        
        // when
        Group result = groupService.updateGroup(testGroup.getId(), updatedGroup);
        
        // then
        assertEquals(testGroup.getId(), result.getId());
        assertEquals("Updated Group Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Updated Image URL", result.getImage());
        
        // Verify that the group was updated in the database
        Group savedGroup = groupRepository.findById(testGroup.getId()).get();
        assertEquals("Updated Group Name", savedGroup.getName());
        assertEquals("Updated Description", savedGroup.getDescription());
        assertEquals("Updated Image URL", savedGroup.getImage());
    }
    
    @Test
    void updateGroup_notFound_throwsException() {
        // given
        Group updatedGroup = new Group();
        updatedGroup.setName("Updated Group Name");
        
        // when/then
        assertThrows(ResponseStatusException.class, () -> groupService.updateGroup(999L, updatedGroup));
        
        // Verify that the original group was not modified
        Group originalGroup = groupRepository.findById(testGroup.getId()).get();
        assertEquals("testGroup", originalGroup.getName());
    }
    
    @Test
    void updateGroup_partialUpdate_success() {
        // given
        assertNotNull(testGroup.getId());
        assertEquals("testGroup", testGroup.getName());
        
        // Create a group with only name updated
        Group updatedGroup = new Group();
        updatedGroup.setName("Updated Group Name");
        
        // when
        Group result = groupService.updateGroup(testGroup.getId(), updatedGroup);
        
        // then
        assertEquals(testGroup.getId(), result.getId());
        assertEquals("Updated Group Name", result.getName());
        
        // Verify that the group was updated in the database
        Group savedGroup = groupRepository.findById(testGroup.getId()).get();
        assertEquals("Updated Group Name", savedGroup.getName());
    }
}
