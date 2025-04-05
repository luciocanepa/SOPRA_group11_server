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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvitationService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMembershipRepository membershipRepository;
    private final GroupService groupService;
    private final UserService userService;

    @Autowired
    public InvitationService(
            @Qualifier("groupRepository") GroupRepository groupRepository,
            @Qualifier("userRepository") UserRepository userRepository,
            @Qualifier("groupMembershipRepository") GroupMembershipRepository membershipRepository,
            GroupService groupService,
            UserService userService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.groupService = groupService;
        this.userService = userService;
    }

    /**
     * Creates an invitation for a user to join a group
     * @param groupId the group ID
     * @param inviterId the user ID of the inviter
     * @param inviteeId the user ID of the invitee
     * @return the created membership entity
     */
    public GroupMembership createInvitation(Long groupId, String token, Long inviteeId) {
        Group group = groupService.getGroupById(groupId);
        User inviter = userService.findByToken(token);

        // The group should contain the inviter
        if (!group.getActiveUsers().contains(inviter)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User with ID " + inviter.getId() + " is not a member of the group");
        }
        
        // The invitee should not be a member of the group
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + inviteeId + " was not found"));
        if (group.getActiveUsers().contains(invitee)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with ID " + inviteeId + " is already a member of the group");
        }
        
        // Check if an invitation already exists
        Optional<GroupMembership> existingMembership = membershipRepository.findByGroupAndUser(group, invitee);
        if (existingMembership.isPresent()) {
            if (existingMembership.get().getStatus() == MembershipStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An invitation for this user and group already exists and is pending");
            } else if (existingMembership.get().getStatus() == MembershipStatus.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The user is already a member of the group");
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An invitation for this user and group already exists and is rejected");
            }
        }
        
        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(invitee);
        membership.setStatus(MembershipStatus.PENDING);
        membership.setInvitedBy(inviter.getId());
        membership.setInvitedAt(LocalDateTime.now());
        
        group.addMembership(membership);
        invitee.addMembership(membership);
        
        membership = membershipRepository.save(membership);
        groupRepository.save(group);
        userRepository.save(invitee);
        
        return membership;
    }
    
    /**
     * Gets all invitations for a user
     * @param userId the user ID
     * @return list of pending invitations
     */
    public List<InvitationGetDTO> getUserInvitations(Long userId, String token) {
        User requestingUser = userService.findByToken(token);
        if (!requestingUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own invitations");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " was not found"));

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
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Group with ID " + groupId + " was not found"));

        User user = userService.findByToken(token);
        if (!group.getActiveUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User with ID " + user.getId() + " is not a member of the group");
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
                    "This invitation does not belong to the user with ID " + user.getId());
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
                        "Invitation with ID " + invitationId + " was not found"));
        
        if (!membership.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "This invitation does not belong to the user with ID " + user.getId());
        }
        
        membership.setStatus(MembershipStatus.REJECTED);
        membershipRepository.save(membership);
    }
} 