package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupMembershipRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.InvitationGetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class InvitationServiceIntegrationTest {

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
    private InvitationService invitationService;

    private User testInviter;
    private User testInvitee;
    private Group testGroup;
    private String testToken;

    @BeforeEach
    void setup() {
        groupRepository.deleteAll();
        userRepository.deleteAll();
        membershipRepository.deleteAll();

        // Create inviter
        testInviter = new User();
        testInviter.setUsername("inviter");
        testInviter.setPassword("password");
        testInviter.setStatus(UserStatus.ONLINE);
        testInviter.setToken("inviter-token");
        testInviter = userService.createUser(testInviter);
        testToken = testInviter.getToken();

        // Create invitee
        testInvitee = new User();
        testInvitee.setUsername("invitee");
        testInvitee.setPassword("password");
        testInvitee.setStatus(UserStatus.ONLINE);
        testInvitee.setToken("invitee-token");
        testInvitee = userService.createUser(testInvitee);

        // Create group
        testGroup = new Group();
        testGroup.setName("testGroup");
        testGroup.setAdminId(testInviter.getId());
        testGroup.setMemberships(new ArrayList<>());
        testGroup = groupService.createGroup(testGroup, testToken);
    }

    @Test
    void createInvitation_validInputs_success() {
        // when
        GroupMembership membership = invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // then
        assertEquals(MembershipStatus.PENDING, membership.getStatus());
        assertEquals(testInviter.getId(), membership.getInvitedBy());
        assertEquals(testInvitee.getId(), membership.getUser().getId());
        assertEquals(testGroup.getId(), membership.getGroup().getId());
    }

    @Test
    void createInvitation_inviterNotInGroup_throwsException() {
        // inviter is not a member of the group
        final User nonMember = new User();
        nonMember.setUsername("nonmember");
        nonMember.setPassword("password");
        nonMember.setStatus(UserStatus.ONLINE);
        nonMember.setToken("nonmember-token");
        userService.createUser(nonMember);

        // then
        Long groupId = testGroup.getId();
        String token = nonMember.getToken();
        Long inviteeId = testInvitee.getId();
        assertThrows(ResponseStatusException.class, () -> 
            invitationService.createInvitation(groupId, token, inviteeId));
    }

    @Test
    void getUserInvitations_validInputs_success() {
        // given
        invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // when
        List<InvitationGetDTO> invitations = invitationService.getUserInvitations(testInvitee.getId(), testInvitee.getToken());

        // then
        assertEquals(1, invitations.size());
        assertEquals(testGroup.getId(), invitations.get(0).getGroupId());
        assertEquals(testInviter.getId(), invitations.get(0).getInviterId());
    }

    @Test
    void getUserInvitations_unauthorizedUser_throwsException() {
        // given
        invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // then
        Long inviteeId = testInvitee.getId();
        assertThrows(ResponseStatusException.class, () -> invitationService.getUserInvitations(inviteeId, testToken));
    }

    @Test
    void getGroupInvitations_validInputs_success() {
        // given
        invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // when
        List<InvitationGetDTO> invitations = invitationService.getGroupInvitations(testGroup.getId(), testToken);

        // then
        assertEquals(1, invitations.size());
        assertEquals(testInvitee.getId(), invitations.get(0).getInviteeId());
        assertEquals(testInviter.getId(), invitations.get(0).getInviterId());
    }

    @Test
    void getGroupInvitations_nonMemberUser_throwsException() {
        // given
        final User nonMember = new User();
        nonMember.setUsername("nonmember");
        nonMember.setPassword("password");
        nonMember.setStatus(UserStatus.ONLINE);
        nonMember.setToken("nonmember-token");
        userService.createUser(nonMember);

        // then
        assertThrows(ResponseStatusException.class, () -> 
            invitationService.getGroupInvitations(testGroup.getId(), nonMember.getToken()));
    }

    @Test
    void getGroupInvitations_memberUser_success() {
        // given
        // Create a regular member (not admin)
        User member = new User();
        member.setUsername("member");
        member.setPassword("password");
        member.setStatus(UserStatus.ONLINE);
        member.setToken("member-token");
        member = userService.createUser(member);
        
        // Add the member to the group through invitation
        GroupMembership memberInvitation = invitationService.createInvitation(testGroup.getId(), testToken, member.getId());
        invitationService.acceptInvitation(memberInvitation.getId(), member.getToken());
        
        // Create an invitation
        invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // when
        List<InvitationGetDTO> invitations = invitationService.getGroupInvitations(testGroup.getId(), member.getToken());

        // then
        assertEquals(1, invitations.size());
        assertEquals(testInvitee.getId(), invitations.get(0).getInviteeId());
        assertEquals(testInviter.getId(), invitations.get(0).getInviterId());
    }

    @Test
    void acceptInvitation_validInputs_success() {
        // given
        GroupMembership membership = invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // when
        Group updatedGroup = invitationService.acceptInvitation(membership.getId(), testInvitee.getToken());

        // then
        // Refresh the group to ensure memberships are loaded
        updatedGroup = groupRepository.findById(updatedGroup.getId()).orElseThrow();
        List<GroupMembership> activeMemberships = membershipRepository.findByGroupAndStatus(updatedGroup, MembershipStatus.ACTIVE);
        assertEquals(2, activeMemberships.size());
        assertTrue(activeMemberships.stream().anyMatch(m -> m.getUser().getId().equals(testInviter.getId())));
        assertTrue(activeMemberships.stream().anyMatch(m -> m.getUser().getId().equals(testInvitee.getId())));
    }

    @Test
    void acceptInvitation_wrongUser_throwsException() {
        // user is not the invitee
        GroupMembership membership = invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // then
        assertThrows(ResponseStatusException.class, () -> 
            invitationService.acceptInvitation(membership.getId(), testToken));
    }

    @Test
    void rejectInvitation_validInputs_success() {
        // given
        GroupMembership membership = invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // when
        invitationService.rejectInvitation(membership.getId(), testInvitee.getToken());

        // then
        GroupMembership updatedMembership = membershipRepository.findById(membership.getId()).orElseThrow();
        assertEquals(MembershipStatus.REJECTED, updatedMembership.getStatus());
    }

    @Test
    void rejectInvitation_wrongUser_throwsException() {
        // user is not the invitee
        GroupMembership membership = invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // then
        assertThrows(ResponseStatusException.class, () -> 
            invitationService.rejectInvitation(membership.getId(), testToken));
    }
}