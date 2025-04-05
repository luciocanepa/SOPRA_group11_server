package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.GroupService;
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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private GroupService groupService;

  @Test
  void givenGroups_whenGetGroups_thenReturnJsonArray() throws Exception {
    // given
    Group group = new Group();
    group.setId(1L);
    group.setName("testGroup");

    List<Group> allGroups = Collections.singletonList(group);

    given(groupService.getGroups()).willReturn(allGroups);

    // when
    MockHttpServletRequestBuilder getRequest = get("/groups").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is(group.getId().intValue())))
        .andExpect(jsonPath("$[0].name", is(group.getName())));
  }

  @Test
  void getGroup_validInput_groupReturned() throws Exception {
    // given
    Group group = new Group();
    group.setId(1L);
    group.setName("testGroup");

    given(groupService.getGroupById(1L)).willReturn(group);

    // when
    MockHttpServletRequestBuilder getRequest = get("/groups/1").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(group.getId().intValue())))
        .andExpect(jsonPath("$.name", is(group.getName())));
  }

  @Test
  void createGroup_validInput_groupCreated() throws Exception {
    // given
    Group group = new Group();
    group.setId(1L);
    group.setName("testGroup");

    GroupPostDTO groupPostDTO = new GroupPostDTO();
    groupPostDTO.setName("testGroup");

    given(groupService.createGroup(Mockito.any())).willReturn(group);

    // when
    MockHttpServletRequestBuilder postRequest = post("/groups")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(groupPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(group.getId().intValue())))
        .andExpect(jsonPath("$.name", is(group.getName())));
  }

  @Test
  void addUserToGroup_validInput_userAdded() throws Exception {
    // given
    Group group = new Group();
    group.setId(1L);
    group.setName("testGroup");

    given(groupService.addUserToGroup(1L, 1L)).willReturn(group);

    // when
    MockHttpServletRequestBuilder postRequest = post("/groups/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("1");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(group.getId().intValue())))
        .andExpect(jsonPath("$.name", is(group.getName())));
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
