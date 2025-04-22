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
import java.util.List;

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
        testGroup.setMemberships(new ArrayList<>());
        testGroup = groupRepository.save(testGroup);

        // Create test membership
        testMembership = new GroupMembership();
        testMembership.setUser(testUser);
        testMembership.setGroup(testGroup);
        testMembership.setStatus(MembershipStatus.ACTIVE);
        testMembership.setInvitedBy(testUser.getId());
        testMembership.setInvitedAt(LocalDateTime.now());
        testMembership = membershipRepository.save(testMembership);
        
        // Set up bidirectional relationships
        testGroup.getMemberships().add(testMembership);
        testUser.getMemberships().add(testMembership);
        
        // Save the entities
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
        Group createdGroup = groupService.createGroup(testGroup, testUser.getToken());

        // then
        assertEquals(testGroup.getName(), createdGroup.getName());
        assertEquals(testUser.getId(), createdGroup.getAdminId());
        assertEquals(1, createdGroup.getActiveUsers().size());
        assertTrue(createdGroup.getActiveUsers().contains(testUser));
    }

    @Test
    void createGroup_invalidAdminId_throwsException() {
        // given
        Group testGroup = new Group();
        testGroup.setName("testGroup");
        testGroup.setAdminId(1L);

        // then
        assertThrows(ResponseStatusException.class, () -> groupService.createGroup(testGroup, "invalid-token"));
    }

    @Test
    void deleteGroup_success() {
        // given
        assertNotNull(testGroup.getId());
        assertNotNull(testMembership.getId());
        assertEquals(1, membershipRepository.findByGroup(testGroup).size());
        
        // when
        groupService.deleteGroup(testGroup.getId(), testUser.getToken());
        
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
        updatedGroup.setAdminId(testUser.getId());
        
        // when
        Group result = groupService.updateGroup(testGroup.getId(), updatedGroup, testUser.getToken());
        
        // then
        assertEquals("Updated Group Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Updated Image URL", result.getImage());
    }

    @Test
    void updateGroup_notFound_throwsException() {
        // given
        Group updatedGroup = new Group();
        updatedGroup.setName("Updated Group Name");
        updatedGroup.setAdminId(testUser.getId());
        
        // then
        assertThrows(ResponseStatusException.class, () -> groupService.updateGroup(999L, updatedGroup, testUser.getToken()));
    }

    @Test
    void updateGroup_partialUpdate_success() {
        // given
        Group updatedGroup = new Group();
        updatedGroup.setName("Updated Group Name");
        updatedGroup.setAdminId(testUser.getId());
        
        // when
        Group result = groupService.updateGroup(testGroup.getId(), updatedGroup, testUser.getToken());
        
        // then
        assertEquals("Updated Group Name", result.getName());
        assertEquals(testGroup.getDescription(), result.getDescription());
        assertEquals(testGroup.getImage(), result.getImage());
    }

    @Test
    void removeUserFromGroup_validInputs_success() {
        // given -> a group with members
        final Group group = new Group();
        group.setName("Test Group");
        group.setDescription("Test Description");
        group.setImage("test.jpg");
        group.setMemberships(new ArrayList<>());
        
        // Create admin user first
        final User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setToken("admin-token");
        adminUser.setStatus(UserStatus.ONLINE);
        adminUser.setMemberships(new ArrayList<>());
        final User savedAdminUser = userRepository.save(adminUser);
        
        // Set admin ID on group before saving
        group.setAdminId(savedAdminUser.getId());
        final Group savedGroup = groupRepository.save(group);
        
        // Create admin membership
        GroupMembership adminMembership = new GroupMembership();
        adminMembership.setUser(savedAdminUser);
        adminMembership.setGroup(savedGroup);
        adminMembership.setStatus(MembershipStatus.ACTIVE);
        adminMembership = membershipRepository.save(adminMembership);
        
        // Add admin membership to group and user
        savedGroup.getMemberships().add(adminMembership);
        savedAdminUser.getMemberships().add(adminMembership);
        groupRepository.save(savedGroup);
        userRepository.save(savedAdminUser);
        
        // Create user to remove
        final User userToRemove = new User();
        userToRemove.setUsername("userToRemove");
        userToRemove.setPassword("password");
        userToRemove.setToken("user-token");
        userToRemove.setStatus(UserStatus.ONLINE);
        userToRemove.setMemberships(new ArrayList<>());
        final User savedUserToRemove = userRepository.save(userToRemove);
        
        // Create membership for user to remove
        GroupMembership membershipToRemove = new GroupMembership();
        membershipToRemove.setUser(savedUserToRemove);
        membershipToRemove.setGroup(savedGroup);
        membershipToRemove.setStatus(MembershipStatus.ACTIVE);
        membershipToRemove = membershipRepository.save(membershipToRemove);
        
        // Add membership to group and user
        savedGroup.getMemberships().add(membershipToRemove);
        savedUserToRemove.getMemberships().add(membershipToRemove);
        groupRepository.save(savedGroup);
        userRepository.save(savedUserToRemove);
        
        // Force initialization of collections before the transaction ends
        membershipRepository.findByGroup(savedGroup);
        membershipRepository.findByUser(savedUserToRemove);
        
        // when -> remove user from group
        groupService.removeUserFromGroup(savedGroup.getId(), savedUserToRemove.getId(), savedAdminUser.getToken());
        
        // then -> verify user is removed from group
        Group updatedGroup = groupRepository.findById(savedGroup.getId()).orElseThrow();
        List<GroupMembership> memberships = membershipRepository.findByGroup(updatedGroup);
        boolean userStillInGroup = memberships.stream()
            .anyMatch(m -> m.getUser().getId().equals(savedUserToRemove.getId()));
        assertFalse(userStillInGroup);
        
        // Verify user's memberships are updated
        List<GroupMembership> userMemberships = membershipRepository.findByUser(savedUserToRemove);
        boolean userHasGroupMembership = userMemberships.stream()
            .anyMatch(m -> m.getGroup().getId().equals(savedGroup.getId()));
        assertFalse(userHasGroupMembership);
    }
    
    @Test
    void removeUserFromGroup_notAdmin_throwsException() {
        // given -> a group with members
        Group group = new Group();
        group.setName("Test Group");
        group.setDescription("Test Description");
        group.setImage("test.jpg");
        group.setMemberships(new ArrayList<>());
        
        // Create admin user first
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setToken("admin-token");
        adminUser.setStatus(UserStatus.ONLINE);
        adminUser.setMemberships(new ArrayList<>());
        final User savedAdminUser = userRepository.save(adminUser);
        
        // Set admin ID on group before saving
        group.setAdminId(savedAdminUser.getId());
        final Group savedGroup = groupRepository.save(group);
        
        // Create admin membership
        GroupMembership adminMembership = new GroupMembership();
        adminMembership.setUser(savedAdminUser);
        adminMembership.setGroup(savedGroup);
        adminMembership.setStatus(MembershipStatus.ACTIVE);
        adminMembership = membershipRepository.save(adminMembership);
        
        // Add admin membership to group and user
        savedGroup.getMemberships().add(adminMembership);
        savedAdminUser.getMemberships().add(adminMembership);
        groupRepository.save(savedGroup);
        userRepository.save(savedAdminUser);
        
        // Create non-admin user
        User nonAdminUser = new User();
        nonAdminUser.setUsername("nonAdmin");
        nonAdminUser.setPassword("password");
        nonAdminUser.setToken("non-admin-token");
        nonAdminUser.setStatus(UserStatus.ONLINE);
        nonAdminUser.setMemberships(new ArrayList<>());
        final User savedNonAdminUser = userRepository.save(nonAdminUser);
        
        // Create non-admin membership
        GroupMembership nonAdminMembership = new GroupMembership();
        nonAdminMembership.setUser(savedNonAdminUser);
        nonAdminMembership.setGroup(savedGroup);
        nonAdminMembership.setStatus(MembershipStatus.ACTIVE);
        nonAdminMembership = membershipRepository.save(nonAdminMembership);
        
        // Add non-admin membership to group and user
        savedGroup.getMemberships().add(nonAdminMembership);
        savedNonAdminUser.getMemberships().add(nonAdminMembership);
        groupRepository.save(savedGroup);
        userRepository.save(savedNonAdminUser);
        
        // Create user to remove
        User userToRemove = new User();
        userToRemove.setUsername("userToRemove");
        userToRemove.setPassword("password");
        userToRemove.setToken("user-token");
        userToRemove.setStatus(UserStatus.ONLINE);
        userToRemove.setMemberships(new ArrayList<>());
        final User savedUserToRemove = userRepository.save(userToRemove);
        
        // Create membership for user to remove
        GroupMembership membershipToRemove = new GroupMembership();
        membershipToRemove.setUser(savedUserToRemove);
        membershipToRemove.setGroup(savedGroup);
        membershipToRemove.setStatus(MembershipStatus.ACTIVE);
        membershipToRemove = membershipRepository.save(membershipToRemove);
        
        // Add membership to group and user
        savedGroup.getMemberships().add(membershipToRemove);
        savedUserToRemove.getMemberships().add(membershipToRemove);
        groupRepository.save(savedGroup);
        userRepository.save(savedUserToRemove);
        
        // Force initialization of collections before the transaction ends
        membershipRepository.findByGroup(savedGroup);
        membershipRepository.findByUser(savedUserToRemove);
        
        // when/then -> attempt to remove user as non-admin -> check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> 
            groupService.removeUserFromGroup(savedGroup.getId(), savedUserToRemove.getId(), savedNonAdminUser.getToken()));
        
        // Verify user is still in group - do this within a new transaction
        Group updatedGroup = groupRepository.findById(savedGroup.getId()).orElseThrow();
        List<GroupMembership> memberships = membershipRepository.findByGroup(updatedGroup);
        boolean userStillInGroup = memberships.stream()
            .anyMatch(m -> m.getUser().getId().equals(savedUserToRemove.getId()));
        assertTrue(userStillInGroup);
    }

    @Test
    void removeUserFromGroup_userNotFound_throwsException() {
        // given -> a group with admin
        final Group group = new Group();
        group.setName("Test Group");
        group.setDescription("Test Description");
        group.setImage("test.jpg");
        group.setMemberships(new ArrayList<>());
        
        // Create admin user first
        final User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setToken("admin-token");
        adminUser.setStatus(UserStatus.ONLINE);
        adminUser.setMemberships(new ArrayList<>());
        final User savedAdminUser = userRepository.save(adminUser);
        
        // Set admin ID on group before saving
        group.setAdminId(savedAdminUser.getId());
        final Group savedGroup = groupRepository.save(group);
        
        // Create admin membership
        GroupMembership adminMembership = new GroupMembership();
        adminMembership.setUser(savedAdminUser);
        adminMembership.setGroup(savedGroup);
        adminMembership.setStatus(MembershipStatus.ACTIVE);
        adminMembership = membershipRepository.save(adminMembership);
        
        // Add admin membership to group and user
        savedGroup.getMemberships().add(adminMembership);
        savedAdminUser.getMemberships().add(adminMembership);
        groupRepository.save(savedGroup);
        userRepository.save(savedAdminUser);
        
        // when/then -> attempt to remove non-existent user -> check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> 
            groupService.removeUserFromGroup(savedGroup.getId(), 999999L, savedAdminUser.getToken()));
    }
    
    @Test
    void removeUserFromGroup_groupNotFound_throwsException() {
        // given -> an admin user
        final User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setToken("admin-token");
        adminUser.setStatus(UserStatus.ONLINE);
        adminUser.setMemberships(new ArrayList<>());
        final User savedAdminUser = userRepository.save(adminUser);
        
        // Create user to remove
        final User userToRemove = new User();
        userToRemove.setUsername("userToRemove");
        userToRemove.setPassword("password");
        userToRemove.setToken("user-token");
        userToRemove.setStatus(UserStatus.ONLINE);
        userToRemove.setMemberships(new ArrayList<>());
        final User savedUserToRemove = userRepository.save(userToRemove);
        
        // when/then -> attempt to remove user from non-existent group -> check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> 
            groupService.removeUserFromGroup(999999L, savedUserToRemove.getId(), savedAdminUser.getToken()));
    }
}
