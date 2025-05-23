package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GroupMembershipRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class MembershipServiceImplTest {

    @Mock
    private GroupMembershipRepository membershipRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MembershipServiceImpl membershipService;

    private User testUser;
    private Group testGroup;
    private GroupMembership testMembership;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Initialize test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        // Initialize test group
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setName("Test Group");

        // Initialize test membership
        testMembership = new GroupMembership();
        testMembership.setId(1L);
        testMembership.setUser(testUser);
        testMembership.setGroup(testGroup);
        testMembership.setStatus(MembershipStatus.ACTIVE);
        testMembership.setInvitedBy(2L);
        testMembership.setInvitedAt(LocalDateTime.now());

        // Setup common mocks
        Mockito.when(membershipRepository.findByGroupAndUser(testGroup, testUser))
                .thenReturn(Optional.of(testMembership));
        Mockito.when(groupRepository.save(any(Group.class))).thenReturn(testGroup);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(testUser);
    }

    @Test
    void addUserToGroup_newMembership_success() {
        // given
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newUser");
        newUser.setMemberships(new ArrayList<>());

        GroupMembership newMembership = new GroupMembership();
        newMembership.setId(2L);
        newMembership.setUser(newUser);
        newMembership.setGroup(testGroup);
        newMembership.setStatus(MembershipStatus.ACTIVE);
        newMembership.setInvitedBy(1L);
        newMembership.setInvitedAt(LocalDateTime.now());

        Mockito.when(membershipRepository.findByGroupAndUser(testGroup, newUser))
                .thenReturn(Optional.empty());
        Mockito.when(membershipRepository.save(any(GroupMembership.class)))
                .thenAnswer(invocation -> {
                    GroupMembership membership = invocation.getArgument(0);
                    membership.setId(2L);
                    return membership;
                });

        // when
        GroupMembership result = membershipService.addUserToGroup(newUser, testGroup,
                MembershipStatus.ACTIVE, 1L);

        // then
        assertNotNull(result);
        assertEquals(newUser, result.getUser());
        assertEquals(testGroup, result.getGroup());
        assertEquals(MembershipStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getInvitedAt());
        assertEquals(1L, result.getInvitedBy());
    }

    @Test
    void addUserToGroup_existingMembership_updatesStatus() {
        // given
        Mockito.when(membershipRepository.save(any(GroupMembership.class)))
                .thenReturn(testMembership);

        // when
        GroupMembership result = membershipService.addUserToGroup(testUser, testGroup,
                MembershipStatus.PENDING, 2L);

        // then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testGroup, result.getGroup());
        assertEquals(MembershipStatus.PENDING, result.getStatus());
    }

    @Test
    void removeUserFromGroup_existingMembership_success() {
        // when
        membershipService.removeUserFromGroup(testUser, testGroup);

        // then
        Mockito.verify(membershipRepository).delete(testMembership);
        Mockito.verify(groupRepository).save(testGroup);
        Mockito.verify(userRepository).save(testUser);
    }

    @Test
    void removeUserFromGroup_nonexistentMembership_noAction() {
        // given
        User nonMemberUser = new User();
        nonMemberUser.setId(999L);
        Mockito.when(membershipRepository.findByGroupAndUser(testGroup, nonMemberUser))
                .thenReturn(Optional.empty());

        // when
        membershipService.removeUserFromGroup(nonMemberUser, testGroup);

        // then
        Mockito.verify(membershipRepository, Mockito.never()).delete(any());
    }

    @Test
    void getActiveUsersInGroup_success() {
        // given
        User activeUser1 = new User();
        activeUser1.setId(1L);
        User activeUser2 = new User();
        activeUser2.setId(2L);

        GroupMembership membership1 = new GroupMembership();
        membership1.setUser(activeUser1);
        membership1.setStatus(MembershipStatus.ACTIVE);

        GroupMembership membership2 = new GroupMembership();
        membership2.setUser(activeUser2);
        membership2.setStatus(MembershipStatus.ACTIVE);

        Mockito.when(membershipRepository.findByGroupAndStatus(testGroup, MembershipStatus.ACTIVE))
                .thenReturn(Arrays.asList(membership1, membership2));

        // when
        List<User> activeUsers = membershipService.getActiveUsersInGroup(testGroup);

        // then
        assertNotNull(activeUsers);
        assertEquals(2, activeUsers.size());
        assertTrue(activeUsers.contains(activeUser1));
        assertTrue(activeUsers.contains(activeUser2));
    }

    @Test
    void getActiveGroupsForUser_success() {
        // given
        Group activeGroup1 = new Group();
        activeGroup1.setId(1L);
        Group activeGroup2 = new Group();
        activeGroup2.setId(2L);

        GroupMembership membership1 = new GroupMembership();
        membership1.setGroup(activeGroup1);
        membership1.setStatus(MembershipStatus.ACTIVE);

        GroupMembership membership2 = new GroupMembership();
        membership2.setGroup(activeGroup2);
        membership2.setStatus(MembershipStatus.ACTIVE);

        Mockito.when(membershipRepository.findByUserAndStatus(testUser, MembershipStatus.ACTIVE))
                .thenReturn(Arrays.asList(membership1, membership2));

        // when
        List<Group> activeGroups = membershipService.getActiveGroupsForUser(testUser);

        // then
        assertNotNull(activeGroups);
        assertEquals(2, activeGroups.size());
        assertTrue(activeGroups.contains(activeGroup1));
        assertTrue(activeGroups.contains(activeGroup2));
    }

    @Test
    void findByUserAndGroup_existingMembership_success() {
        // when
        GroupMembership result = membershipService.findByUserAndGroup(testUser, testGroup);

        // then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testGroup, result.getGroup());
    }

    @Test
    void findByUserAndGroup_nonexistentMembership_returnsNull() {
        // given
        User nonMemberUser = new User();
        nonMemberUser.setId(999L);
        Mockito.when(membershipRepository.findByGroupAndUser(testGroup, nonMemberUser))
                .thenReturn(Optional.empty());

        // when
        GroupMembership result = membershipService.findByUserAndGroup(nonMemberUser, testGroup);

        // then
        assertNull(result);
    }

    @Test
    void updateMembershipStatus_success() {
        // given
        Mockito.when(membershipRepository.save(any(GroupMembership.class)))
                .thenReturn(testMembership);

        // when
        GroupMembership result = membershipService.updateMembershipStatus(testMembership,
                MembershipStatus.PENDING);

        // then
        assertNotNull(result);
        assertEquals(MembershipStatus.PENDING, result.getStatus());
        Mockito.verify(membershipRepository).save(testMembership);
    }
}