package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class GroupMembershipRepositoryTest {

    @Qualifier("groupMembershipRepository")
    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Qualifier("groupRepository")
    @Autowired
    private GroupRepository groupRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

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
        
        // Flush to ensure all changes are persisted
        membershipRepository.flush();
        groupRepository.flush();
        userRepository.flush();
    }

    @Test
    void findByUser_success() {
        // when
        List<GroupMembership> memberships = membershipRepository.findByUser(testUser);

        // then
        assertEquals(1, memberships.size());
        assertEquals(testMembership.getId(), memberships.get(0).getId());
        assertEquals(testUser.getId(), memberships.get(0).getUser().getId());
    }

    @Test
    void findByGroup_success() {
        // when
        List<GroupMembership> memberships = membershipRepository.findByGroup(testGroup);

        // then
        assertEquals(1, memberships.size());
        assertEquals(testMembership.getId(), memberships.get(0).getId());
        assertEquals(testGroup.getId(), memberships.get(0).getGroup().getId());
    }

    @Test
    void findByUserAndStatus_success() {
        // when
        List<GroupMembership> memberships = membershipRepository.findByUserAndStatus(testUser, MembershipStatus.ACTIVE);

        // then
        assertEquals(1, memberships.size());
        assertEquals(testMembership.getId(), memberships.get(0).getId());
        assertEquals(MembershipStatus.ACTIVE, memberships.get(0).getStatus());
    }

    @Test
    void findByUserAndStatus_noResults() {
        // when
        List<GroupMembership> memberships = membershipRepository.findByUserAndStatus(testUser, MembershipStatus.PENDING);

        // then
        assertTrue(memberships.isEmpty());
    }

    @Test
    void findByGroupAndStatus_success() {
        // when
        List<GroupMembership> memberships = membershipRepository.findByGroupAndStatus(testGroup, MembershipStatus.ACTIVE);

        // then
        assertEquals(1, memberships.size());
        assertEquals(testMembership.getId(), memberships.get(0).getId());
        assertEquals(MembershipStatus.ACTIVE, memberships.get(0).getStatus());
    }

    @Test
    void findByGroupAndStatus_noResults() {
        // when
        List<GroupMembership> memberships = membershipRepository.findByGroupAndStatus(testGroup, MembershipStatus.PENDING);

        // then
        assertTrue(memberships.isEmpty());
    }

    @Test
    void findByGroupAndUser_success() {
        // when
        Optional<GroupMembership> membership = membershipRepository.findByGroupAndUser(testGroup, testUser);

        // then
        assertTrue(membership.isPresent());
        assertEquals(testMembership.getId(), membership.get().getId());
        assertEquals(testUser.getId(), membership.get().getUser().getId());
        assertEquals(testGroup.getId(), membership.get().getGroup().getId());
    }

    @Test
    void findByGroupAndUser_noResults() {
        // given
        User otherUser = new User();
        otherUser.setUsername("otherUser");
        otherUser.setPassword("password");
        otherUser.setToken("other-token");
        otherUser.setStatus(UserStatus.ONLINE);
        otherUser = userRepository.save(otherUser);

        // when
        Optional<GroupMembership> membership = membershipRepository.findByGroupAndUser(testGroup, otherUser);

        // then
        assertFalse(membership.isPresent());
    }
} 