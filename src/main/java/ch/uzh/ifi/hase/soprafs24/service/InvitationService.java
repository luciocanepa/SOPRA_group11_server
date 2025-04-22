package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GroupMembershipRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.InvitationGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InvitationService {

    private final UserRepository userRepository;
    private final GroupMembershipRepository membershipRepository;
    private final GroupService groupService;
    private final UserService userService;
    private final MembershipService membershipService;

    private static final String NOT_FOUND = "%s with ID %s was not found";
    private static final String FORBIDDEN = "User with ID %s is not authorized to perform this action";
    private static final String CONFLICT = "User with ID %s is already a member of the group";
    private static final String INVITATION_EXISTS = "Invitation for user with ID %s to group with ID %s already exists";

    @Autowired
    public InvitationService(
            @Qualifier("groupRepository") GroupRepository groupRepository,
            @Qualifier("userRepository") UserRepository userRepository,
            @Qualifier("groupMembershipRepository") GroupMembershipRepository membershipRepository,
            GroupService groupService,
            UserService userService,
            MembershipService membershipService) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.groupService = groupService;
        this.userService = userService;
        this.membershipService = membershipService;
    }

    /**
     * Creates an invitation for a user to join a group
     * @param groupId the group ID
     * @param inviterId the user ID of the inviter
     * @param inviteeId the user ID of the invitee
     * @return the created membership entity
     */
    public GroupMembership createInvitation(Long groupId, String token, Long inviteeId) {
        Group group = groupService.getGroupById(groupId, token);
        User inviter = userService.findByToken(token);

        // The group should contain the inviter
        if (!group.getActiveUsers().contains(inviter)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format(FORBIDDEN, inviter.getId()));
        }
        
        // The invitee should not be a member of the group
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(NOT_FOUND, "User", inviteeId)));
        if (group.getActiveUsers().contains(invitee)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(CONFLICT, inviteeId));
        }
        
        // Check if an invitation already exists
        GroupMembership existingMembership = membershipService.findByUserAndGroup(invitee, group);
        if (existingMembership != null) {
            if (existingMembership.getStatus() == MembershipStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An invitation for this user and group already exists and is pending");
            } else if (existingMembership.getStatus() == MembershipStatus.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The user is already a member of the group");
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An invitation for this user and group already exists and is rejected");
            }
        }
        
        // Create the invitation using the membership service
        return membershipService.addUserToGroup(invitee, group, MembershipStatus.PENDING, inviter.getId());
    }
    
    /**
     * Gets all invitations for a user
     * @param userId the user ID
     * @return list of pending invitations
     */
    public List<InvitationGetDTO> getUserInvitations(Long userId, String token) {
        User requestingUser = userService.findByToken(token);
        if (!requestingUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authorized to view these invitations");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(NOT_FOUND, "User", userId)));

        List<InvitationGetDTO> invitations = new ArrayList<>();
        List<GroupMembership> pendingMemberships = membershipRepository.findByUserAndStatus(user, MembershipStatus.PENDING);
        for (GroupMembership membership : pendingMemberships) {
            InvitationGetDTO dto = DTOMapper.INSTANCE.convertMembershipToInvitationGetDTO(membership);
            invitations.add(dto);
        }
        return invitations;
    }

    /** 
     * Gets all pending invitations for a group
     * @param groupId the group ID
     * @return list of pending invitations
     */
    public List<InvitationGetDTO> getGroupInvitations(Long groupId, String token) {
        Group group = groupService.getGroupById(groupId, token);
        User requestingUser = userService.findByToken(token);
        
        // Check if the requesting user is a member of the group
        boolean isMember = group.getActiveUsers().contains(requestingUser);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format(FORBIDDEN, requestingUser.getId()));
        }
        
        List<InvitationGetDTO> invitations = new ArrayList<>();
        List<GroupMembership> pendingMemberships = membershipRepository.findByGroupAndStatus(group, MembershipStatus.PENDING);
        for (GroupMembership membership : pendingMemberships) {
            InvitationGetDTO dto = DTOMapper.INSTANCE.convertMembershipToInvitationGetDTO(membership);
            invitations.add(dto);
        }
        return invitations;
    }
    
    /**
     * Accepts an invitation
     * @param invitationId the invitation ID
     * @param userId the user ID who is accepting
     * @return the updated group
     */
    public Group acceptInvitation(Long invitationId, String token) {
        User user = userService.findByToken(token);

        GroupMembership membership = membershipRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation with ID " + invitationId + " was not found"));
        
        if (!membership.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    String.format(FORBIDDEN, user.getId()));
        }
        
        membership.setStatus(MembershipStatus.ACTIVE);
        membershipRepository.save(membership);
        
        return membership.getGroup();
    }
    
    /**
     * Rejects an invitation
     * @param invitationId the invitation ID
     * @param userId the user ID who is rejecting
     */
    public void rejectInvitation(Long invitationId, String token) {
        User user = userService.findByToken(token);
        GroupMembership membership = membershipRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        String.format(NOT_FOUND, "Invitation", invitationId)));
        
        if (!membership.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    String.format(FORBIDDEN, user.getId()));
        }
        
        membershipService.removeUserFromGroup(membership.getUser(), membership.getGroup());
    }
} 