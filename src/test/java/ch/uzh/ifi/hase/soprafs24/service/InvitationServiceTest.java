package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupMembershipRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.InvitationGetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InvitationServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMembershipRepository membershipRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private InvitationService invitationService;

    private Group testGroup;
    private User testInviter;
    private User testInvitee;
    private GroupMembership testMembership;
    private String testToken;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testToken = "test-token";
        testInviter = new User();
        testInviter.setId(1L);
        testInviter.setUsername("inviter");
        testInviter.setToken(testToken);
        testInviter.setMemberships(new ArrayList<>());

        testInvitee = new User();
        testInvitee.setId(2L);
        testInvitee.setUsername("invitee");
        testInvitee.setMemberships(new ArrayList<>());

        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setName("testGroup");
        testGroup.setAdminId(testInviter.getId());
        testGroup.setMemberships(new ArrayList<>());

        // Create and add active membership for inviter
        GroupMembership inviterMembership = new GroupMembership();
        inviterMembership.setUser(testInviter);
        inviterMembership.setGroup(testGroup);
        inviterMembership.setStatus(MembershipStatus.ACTIVE);
        inviterMembership.setInvitedBy(testInviter.getId());
        inviterMembership.setInvitedAt(LocalDateTime.now());
        testGroup.getMemberships().add(inviterMembership);
        testInviter.getMemberships().add(inviterMembership);

        testMembership = new GroupMembership();
        testMembership.setId(1L);
        testMembership.setUser(testInvitee);
        testMembership.setGroup(testGroup);
        testMembership.setStatus(MembershipStatus.PENDING);
        testMembership.setInvitedBy(testInviter.getId());
        testMembership.setInvitedAt(LocalDateTime.now());

        // Setup common mocks
        Mockito.when(userService.findByToken(testToken)).thenReturn(testInviter);
        Mockito.when(groupService.getGroupById(testGroup.getId())).thenReturn(testGroup);
        Mockito.when(userRepository.findById(testInvitee.getId())).thenReturn(Optional.of(testInvitee));
        Mockito.when(membershipRepository.save(Mockito.any())).thenReturn(testMembership);
        Mockito.when(groupRepository.save(Mockito.any())).thenReturn(testGroup);
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testInvitee);
        Mockito.when(membershipService.findByUserAndGroup(Mockito.any(), Mockito.any())).thenReturn(null);
        Mockito.when(membershipService.addUserToGroup(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(testMembership);
    }

    @Test
    void createInvitation_validInputs_success() {
        // then
        GroupMembership createdMembership = invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId());

        // verify
        Mockito.verify(membershipService).addUserToGroup(Mockito.any(), Mockito.any(), Mockito.eq(MembershipStatus.PENDING), Mockito.eq(testInviter.getId()));

        assertEquals(MembershipStatus.PENDING, createdMembership.getStatus());
        assertEquals(testInviter.getId(), createdMembership.getInvitedBy());
        assertEquals(testInvitee.getId(), createdMembership.getUser().getId());
        assertEquals(testGroup.getId(), createdMembership.getGroup().getId());
    }

    @Test
    void createInvitation_inviterNotInGroup_throwsException() {
        // If inviter is not a member of the group, return empty membership
        testGroup.setMemberships(new ArrayList<>());

        // then
        assertThrows(ResponseStatusException.class, () -> 
            invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId()));
    }

    @Test
    void createInvitation_inviteeAlreadyMember_throwsException() {
        // If invitee is already a member of the group, return existing membership
        GroupMembership existingMembership = new GroupMembership();
        existingMembership.setStatus(MembershipStatus.ACTIVE);
        Mockito.when(membershipService.findByUserAndGroup(testInvitee, testGroup))
                .thenReturn(existingMembership);

        // then
        assertThrows(ResponseStatusException.class, () -> 
            invitationService.createInvitation(testGroup.getId(), testToken, testInvitee.getId()));
    }

    @Test
    void getUserInvitations_validInputs_success() {
        // get all pending invitations for the invitee (if any and if user exists)
        List<GroupMembership> pendingMemberships = List.of(testMembership);
        Mockito.when(membershipRepository.findByUserAndStatus(testInvitee, MembershipStatus.PENDING))
                .thenReturn(pendingMemberships);
        Mockito.when(userRepository.findById(testInvitee.getId()))
                .thenReturn(Optional.of(testInvitee));
        
        // invitee has access
        String inviteeToken = "invitee-token";
        Mockito.when(userService.findByToken(inviteeToken)).thenReturn(testInvitee);

        // then
        List<InvitationGetDTO> invitations = invitationService.getUserInvitations(testInvitee.getId(), inviteeToken);

        // verify
        assertEquals(1, invitations.size());
        assertEquals(testMembership.getId(), invitations.get(0).getId());
    }

    @Test
    void getUserInvitations_unauthorizedUser_throwsException() {
        // user does not have access
        User unauthorizedUser = new User();
        unauthorizedUser.setId(999L);
        Mockito.when(userService.findByToken(testToken)).thenReturn(unauthorizedUser);

        // then
        assertThrows(ResponseStatusException.class, () -> 
            invitationService.getUserInvitations(testInvitee.getId(), testToken));
    }

    @Test
    void getGroupInvitations_validInputs_success() {
        // get all pending invitations for the group (if any and if group exists)
        List<GroupMembership> pendingMemberships = List.of(testMembership);
        Mockito.when(membershipRepository.findByGroupAndStatus(testGroup, MembershipStatus.PENDING))
                .thenReturn(pendingMemberships);

        // then
        List<InvitationGetDTO> invitations = invitationService.getGroupInvitations(testGroup.getId());

        // verify
        assertEquals(1, invitations.size());
        assertEquals(testMembership.getId(), invitations.get(0).getId());
    }

    @Test
    void getGroupInvitations_userNotInGroup_success() {
        // user is not a member of the group
        testGroup.setMemberships(new ArrayList<>());
        List<GroupMembership> emptyList = new ArrayList<>();
        Mockito.when(membershipRepository.findByGroupAndStatus(testGroup, MembershipStatus.PENDING))
                .thenReturn(emptyList);

        // then
        List<InvitationGetDTO> invitations = invitationService.getGroupInvitations(testGroup.getId());
        
        // verify
        assertEquals(0, invitations.size());
    }

    @Test
    void acceptInvitation_validInputs_success() {
        // find the membership (if any)
        Mockito.when(membershipRepository.findById(testMembership.getId()))
                .thenReturn(Optional.of(testMembership));
        Mockito.when(membershipRepository.save(Mockito.any(GroupMembership.class)))
                .thenAnswer(invocation -> {
                    GroupMembership membership = invocation.getArgument(0);
                    membership.setStatus(MembershipStatus.ACTIVE);
                    return membership;
                });
        
        // invitee has access
        String inviteeToken = "invitee-token";
        Mockito.when(userService.findByToken(inviteeToken)).thenReturn(testInvitee);

        // then
        Group updatedGroup = invitationService.acceptInvitation(testMembership.getId(), inviteeToken);

        // verify
        Mockito.verify(membershipRepository).save(Mockito.any());
        assertEquals(MembershipStatus.ACTIVE, testMembership.getStatus());
        assertEquals(testGroup.getId(), updatedGroup.getId());
    }

    @Test
    void acceptInvitation_wrongUser_throwsException() {
        // invitee does not have access
        User wrongUser = new User();
        wrongUser.setId(999L);
        Mockito.when(userService.findByToken(testToken)).thenReturn(wrongUser);
        Mockito.when(membershipRepository.findById(testMembership.getId()))
                .thenReturn(Optional.of(testMembership));

        // then
        assertThrows(ResponseStatusException.class, () -> 
            invitationService.acceptInvitation(testMembership.getId(), testToken));
    }

    @Test
    void rejectInvitation_validInputs_success() {
        // find the membership (if any)
        Mockito.when(membershipRepository.findById(testMembership.getId()))
                .thenReturn(Optional.of(testMembership));
        Mockito.when(membershipRepository.save(Mockito.any(GroupMembership.class)))
                .thenAnswer(invocation -> {
                    GroupMembership membership = invocation.getArgument(0);
                    membership.setStatus(MembershipStatus.REJECTED);
                    return membership;
                });
        
        // invitee has access
        String inviteeToken = "invitee-token";
        Mockito.when(userService.findByToken(inviteeToken)).thenReturn(testInvitee);

        // then
        invitationService.rejectInvitation(testMembership.getId(), inviteeToken);

        // verify
        Mockito.verify(membershipRepository).save(Mockito.any());
        assertEquals(MembershipStatus.REJECTED, testMembership.getStatus());
    }

    @Test
    void rejectInvitation_wrongUser_throwsException() {
        // invitee does not have access
        User wrongUser = new User();
        wrongUser.setId(999L);
        Mockito.when(userService.findByToken(testToken)).thenReturn(wrongUser);
        Mockito.when(membershipRepository.findById(testMembership.getId()))
                .thenReturn(Optional.of(testMembership));

        // then
        assertThrows(ResponseStatusException.class, () -> 
            invitationService.rejectInvitation(testMembership.getId(), testToken));
    }
} 