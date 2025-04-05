package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.InvitationGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.InvitationPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.InvitationService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Invitation Controller
 * This class is responsible for handling all REST requests related to group invitations.
 */
@RestController
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;

    InvitationController(InvitationService invitationService, UserService userService) {
        this.invitationService = invitationService;
        this.userService = userService;
    }

    /**
     * POST /groups/{gid}/invitations : Send an invitation to a user to join a group
     */
    @PostMapping("/groups/{gid}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationGetDTO createInvitation(@PathVariable Long gid, @RequestBody InvitationPostDTO invitationPostDTO, @RequestHeader("Authorization") String token) {
        GroupMembership membership = invitationService.createInvitation(gid, token, invitationPostDTO.getInviteeId());
        return DTOMapper.INSTANCE.convertMembershipToInvitationGetDTO(membership);
    }

    @GetMapping("/groups/{gid}/invitations")
    @ResponseStatus(HttpStatus.OK)
    public List<InvitationGetDTO> getGroupInvitations(@PathVariable Long gid, @RequestHeader("Authorization") String token) {
        return invitationService.getGroupInvitations(gid);
    }


    @GetMapping("/users/{uid}/invitations")
    @ResponseStatus(HttpStatus.OK)
    public List<InvitationGetDTO> getUserInvitations(@PathVariable Long uid, @RequestHeader("Authorization") String token) {
        return invitationService.getUserInvitations(uid, token);
    }

    /**
     * PUT /invitations/{iid}/accept : Accept an invitation
     */
    @PutMapping("/invitations/{iid}/accept")
    @ResponseStatus(HttpStatus.OK)
    public GroupGetDTO acceptInvitation(@PathVariable Long iid, @RequestHeader("Authorization") String token) {
        Group updatedGroup = invitationService.acceptInvitation(iid, token);
        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(updatedGroup);
    }

    /**
     * PUT /invitations/{iid}/reject : Reject an invitation
     */
    @PutMapping("/invitations/{iid}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectInvitation(@PathVariable Long iid, @RequestHeader("Authorization") String token) {
        invitationService.rejectInvitation(iid, token);
    }
}