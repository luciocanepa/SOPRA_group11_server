package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Activity;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ActivityPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ActivityGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ActivityAggregateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserActivitiesGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserAggregatedActivitiesGetDTO;
import ch.uzh.ifi.hase.soprafs24.service.ActivityService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
public class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

    @MockBean
    private UserService userService;

    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void createActivity_validInput_activityCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setToken("test-token");

        ActivityPostDTO activityPostDTO = new ActivityPostDTO();
        activityPostDTO.setStartDateTime(LocalDateTime.now());
        activityPostDTO.setEndDateTime(LocalDateTime.now().plusHours(1));

        Activity createdActivity = new Activity();
        createdActivity.setId(1L);
        createdActivity.setUser(user);
        createdActivity.setStartDateTime(activityPostDTO.getStartDateTime());
        createdActivity.setEndDateTime(activityPostDTO.getEndDateTime());

        given(activityService.createActivity(any(), eq(1L), eq("test-token"))).willReturn(createdActivity);

        // when/then
        MockHttpServletRequestBuilder postRequest = post("/users/1/statistics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(activityPostDTO))
                .header("Authorization", "test-token");

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    public void createActivity_invalidToken_throwsException() throws Exception {
        // given
        ActivityPostDTO activityPostDTO = new ActivityPostDTO();
        activityPostDTO.setStartDateTime(LocalDateTime.now());
        activityPostDTO.setEndDateTime(LocalDateTime.now().plusHours(1));

        given(activityService.createActivity(any(), eq(1L), eq("invalid-token")))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        // when/then
        MockHttpServletRequestBuilder postRequest = post("/users/1/statistics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(activityPostDTO))
                .header("Authorization", "invalid-token");

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getActivitiesByUserId_validInput_activitiesReturned() throws Exception {
        // given
        User user = new User();
        user.setId(1L);

        Activity activity = new Activity();
        activity.setId(1L);
        activity.setUser(user);
        activity.setStartDateTime(LocalDateTime.now());
        activity.setEndDateTime(LocalDateTime.now().plusHours(1));

        List<Activity> activities = Collections.singletonList(activity);
        given(activityService.getActivitiesByDateRange(eq(1L), eq("test-token"), any(), any()))
                .willReturn(activities);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/users/1/statistics")
                .header("Authorization", "test-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    public void getActivitiesByUserId_withAggregation_aggregatedActivitiesReturned() throws Exception {
        // given
        ActivityAggregateDTO aggregateDTO = new ActivityAggregateDTO();
        aggregateDTO.setDate(LocalDate.now());
        aggregateDTO.setDuration(120L); // 2 hours

        List<ActivityAggregateDTO> aggregatedActivities = Collections.singletonList(aggregateDTO);
        given(activityService.getAggregatedActivities(eq(1L), eq("test-token"), any(), any()))
                .willReturn(aggregatedActivities);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/users/1/statistics")
                .param("aggregate", "true")
                .header("Authorization", "test-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].duration", is(120)));
    }

    @Test
    public void getActivitiesByGroupId_validInput_activitiesReturned() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setToken("test-token");

        given(userService.findByToken("test-token")).willReturn(user);

        UserActivitiesGetDTO userActivities = new UserActivitiesGetDTO();
        userActivities.setUserId(1L);
        userActivities.setUsername("testUser");
        List<ActivityGetDTO> activities = new ArrayList<>();
        userActivities.setActivities(activities);

        List<UserActivitiesGetDTO> groupActivities = Collections.singletonList(userActivities);
        given(activityService.getGroupUsersActivities(eq(1L), eq("test-token"), eq(1L), any(), any()))
                .willReturn(groupActivities);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/groups/1/statistics")
                .header("Authorization", "test-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].username", is("testUser")));
    }

    @Test
    public void getActivitiesByGroupId_withAggregation_aggregatedActivitiesReturned() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setToken("test-token");

        given(userService.findByToken("test-token")).willReturn(user);

        UserAggregatedActivitiesGetDTO userAggregated = new UserAggregatedActivitiesGetDTO();
        userAggregated.setUserId(1L);
        userAggregated.setUsername("testUser");
        List<ActivityAggregateDTO> aggregatedActivities = new ArrayList<>();
        userAggregated.setAggregatedActivities(aggregatedActivities);

        List<UserAggregatedActivitiesGetDTO> groupAggregated = Collections.singletonList(userAggregated);
        given(activityService.getGroupUsersAggregatedActivities(eq(1L), eq("test-token"), eq(1L), any(), any()))
                .willReturn(groupAggregated);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/groups/1/statistics")
                .param("aggregate", "true")
                .header("Authorization", "test-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].username", is("testUser")));
    }

    @Test
    public void getActivitiesByGroupId_invalidToken_throwsException() throws Exception {
        // given
        given(userService.findByToken("invalid-token"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/groups/1/statistics")
                .header("Authorization", "invalid-token");

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    /**
     * Helper Method to convert activityPostDTO into a JSON string such that the input
     * can be processed Input will look like this: {"name": "Test User", "username":
     * "testUsername"}
     */
    private String asJsonString(final Object object) {
        try {
            return mapper.writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
} 