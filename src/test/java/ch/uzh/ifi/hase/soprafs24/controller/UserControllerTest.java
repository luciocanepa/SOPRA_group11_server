package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    String validToken = "valid-token";
    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers(validToken)).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", validToken);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("testPassword");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  @Test
  void createUser_duplicateUsername_throwsException() throws Exception {
    //given
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("testPassword");

    given(userService.createUser(Mockito.any()))
    .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, 
    "The username provided is not unique. Therefore, the user could not be created!"));

    //when
    MockHttpServletRequestBuilder postRequest = post("/users/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));
    
    mockMvc.perform(postRequest)
    .andExpect(status().isConflict());

  }
  
    @Test
    void getUserGroups_validUserId_groupsReturned() throws Exception {
        // given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testUsername");
        user.setStatus(UserStatus.ONLINE);

        Group group1 = new Group();
        group1.setName("Group 1");

        Group group2 = new Group();
        group2.setName("Group 2");

        List<Group> activeGroups = Arrays.asList(group1, group2);

        String validToken = "valid-token";
        // This mocks the UserService -> define what it should return
        given(userService.getGroupsForUser(userId, validToken)).willReturn(activeGroups);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/groups", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", validToken);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))  // Check if two groups are returned
                .andExpect(jsonPath("$[0].name", is(group1.getName())))
                .andExpect(jsonPath("$[1].name", is(group2.getName())));
    }

    @Test
    void getUserGroups_userNotFound_throwsNotFoundException() throws Exception {
        // given
        Long nonExistentUserId = 999L;  // This user ID does not exist in the database
        String validToken = "valid-token";

        // Mock the UserService to throw a ResponseStatusException for the non-existent user
        given(userService.getGroupsForUser(nonExistentUserId, validToken))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/groups", nonExistentUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", validToken);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());  // Expect 404 Not Found
    }
  /*
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}