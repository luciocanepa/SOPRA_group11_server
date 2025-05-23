package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.InvitationGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.InvitationPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.InvitationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationController.class)
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvitationService invitationService;

    @Test
    void createInvitation_validInput_invitationCreated() throws Exception {
        // given
        Long groupId = 1L;
        Long inviteeId = 2L;
        String token = "valid-token";

        GroupMembership membership = new GroupMembership();
        membership.setId(1L);
        Group group = new Group();
        group.setId(groupId);
        membership.setGroup(group);
        User user = new User();
        user.setId(inviteeId);
        membership.setUser(user);

        InvitationPostDTO invitationPostDTO = new InvitationPostDTO();
        invitationPostDTO.setInviteeId(inviteeId);

        given(invitationService.createInvitation(Mockito.any(), Mockito.any(), Mockito.any()))
                .willReturn(membership);

        // when/then
        MockHttpServletRequestBuilder postRequest = post("/groups/{gid}/invitations", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(asJsonString(invitationPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(membership.getId().intValue())))
                .andExpect(jsonPath("$.inviteeId", is(user.getId().intValue())));
    }

    @Test
    void createInvitation_invalidGroup_throwsException() throws Exception {
        // given
        Long invalidGroupId = 999L;
        Long inviteeId = 2L;
        String token = "valid-token";

        InvitationPostDTO invitationPostDTO = new InvitationPostDTO();
        invitationPostDTO.setInviteeId(inviteeId);

        given(invitationService.createInvitation(Mockito.any(), Mockito.any(), Mockito.any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // when/then
        MockHttpServletRequestBuilder postRequest = post("/groups/{gid}/invitations", invalidGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(asJsonString(invitationPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    void getGroupInvitations_validInput_invitationsReturned() throws Exception {
        // given
        Long groupId = 1L;
        String token = "valid-token";

        GroupMembership membership1 = new GroupMembership();
        membership1.setId(1L);
        GroupMembership membership2 = new GroupMembership();
        membership2.setId(2L);

        List<InvitationGetDTO> invitations = Arrays.asList(
                DTOMapper.INSTANCE.convertMembershipToInvitationGetDTO(membership1),
                DTOMapper.INSTANCE.convertMembershipToInvitationGetDTO(membership2));

        given(invitationService.getGroupInvitations(groupId, token)).willReturn(invitations);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/groups/{gid}/invitations", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getUserInvitations_validInput_invitationsReturned() throws Exception {
        // given
        Long userId = 1L;
        String token = "valid-token";

        GroupMembership membership1 = new GroupMembership();
        membership1.setId(1L);
        GroupMembership membership2 = new GroupMembership();
        membership2.setId(2L);

        List<InvitationGetDTO> invitations = Arrays.asList(
                DTOMapper.INSTANCE.convertMembershipToInvitationGetDTO(membership1),
                DTOMapper.INSTANCE.convertMembershipToInvitationGetDTO(membership2));

        given(invitationService.getUserInvitations(userId, token)).willReturn(invitations);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/users/{uid}/invitations", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void acceptInvitation_validInput_invitationAccepted() throws Exception {
        // given
        Long invitationId = 1L;
        String token = "valid-token";

        Group group = new Group();
        group.setId(1L);
        group.setName("Test Group");

        given(invitationService.acceptInvitation(invitationId, token)).willReturn(group);

        // when/then
        MockHttpServletRequestBuilder putRequest = put("/invitations/{iid}/accept", invitationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(group.getId().intValue())))
                .andExpect(jsonPath("$.name", is(group.getName())));
    }

    @Test
    void acceptInvitation_invalidInvitation_throwsException() throws Exception {
        // given
        Long invalidInvitationId = 999L;
        String token = "valid-token";

        given(invitationService.acceptInvitation(invalidInvitationId, token))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        // when/then
        MockHttpServletRequestBuilder putRequest = put("/invitations/{iid}/accept", invalidInvitationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectInvitation_validInput_invitationRejected() throws Exception {
        // given
        Long invitationId = 1L;
        String token = "valid-token";

        // when/then
        MockHttpServletRequestBuilder putRequest = put("/invitations/{iid}/reject", invitationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectInvitation_invalidInvitation_throwsException() throws Exception {
        // given
        Long invalidInvitationId = 999L;
        String token = "valid-token";

        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"))
                .when(invitationService).rejectInvitation(invalidInvitationId, token);

        // when/then
        MockHttpServletRequestBuilder putRequest = put("/invitations/{iid}/reject", invalidInvitationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}