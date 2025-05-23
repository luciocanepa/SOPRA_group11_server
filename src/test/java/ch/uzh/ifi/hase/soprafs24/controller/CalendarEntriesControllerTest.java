package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.CalendarEntries;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CalendarEntriesPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.CalendarEntryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarEntriesController.class)
class CalendarEntriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarEntryService calendarEntryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void createCalendarEntry_validInput_entryCreated() throws Exception {
        // given
        Long groupId = 1L;
        String token = "valid-token";

        CalendarEntriesPostDTO postDTO = new CalendarEntriesPostDTO();
        postDTO.setTitle("Test Event");
        postDTO.setDescription("Test Description");
        postDTO.setStartTime(LocalDateTime.now());
        postDTO.setEndTime(LocalDateTime.now().plusHours(1));

        CalendarEntries createdEntry = new CalendarEntries();
        createdEntry.setId(1L);
        createdEntry.setTitle(postDTO.getTitle());
        createdEntry.setDescription(postDTO.getDescription());
        createdEntry.setStartTime(postDTO.getStartTime());
        createdEntry.setEndTime(postDTO.getEndTime());

        given(calendarEntryService.createEntry(Mockito.any(), Mockito.any(), Mockito.any()))
                .willReturn(createdEntry);

        // when/then
        MockHttpServletRequestBuilder postRequest = post("/groups/{groupId}/calendar-entries", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(asJsonString(postDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(createdEntry.getId().intValue())))
                .andExpect(jsonPath("$.title", is(createdEntry.getTitle())))
                .andExpect(jsonPath("$.description", is(createdEntry.getDescription())));
    }

    @Test
    void createCalendarEntry_invalidGroup_throwsException() throws Exception {
        // given
        Long invalidGroupId = 999L;
        String token = "valid-token";

        CalendarEntriesPostDTO postDTO = new CalendarEntriesPostDTO();
        postDTO.setTitle("Test Event");
        postDTO.setDescription("Test Description");
        postDTO.setStartTime(LocalDateTime.now());
        postDTO.setEndTime(LocalDateTime.now().plusHours(1));

        given(calendarEntryService.createEntry(Mockito.any(), Mockito.any(), Mockito.any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // when/then
        MockHttpServletRequestBuilder postRequest = post("/groups/{groupId}/calendar-entries", invalidGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(asJsonString(postDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    void getCalendarEntriesForGroup_validInput_entriesReturned() throws Exception {
        // given
        Long groupId = 1L;
        String token = "valid-token";

        CalendarEntries entry1 = new CalendarEntries();
        entry1.setId(1L);
        entry1.setTitle("Event 1");
        entry1.setDescription("Description 1");
        entry1.setStartTime(LocalDateTime.now());
        entry1.setEndTime(LocalDateTime.now().plusHours(1));

        CalendarEntries entry2 = new CalendarEntries();
        entry2.setId(2L);
        entry2.setTitle("Event 2");
        entry2.setDescription("Description 2");
        entry2.setStartTime(LocalDateTime.now().plusHours(2));
        entry2.setEndTime(LocalDateTime.now().plusHours(3));

        List<CalendarEntries> entries = Arrays.asList(entry1, entry2);

        given(calendarEntryService.getCalendarEntriesForGroup(groupId, token)).willReturn(entries);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/calendar-entries", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(entry1.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is(entry1.getTitle())))
                .andExpect(jsonPath("$[1].id", is(entry2.getId().intValue())))
                .andExpect(jsonPath("$[1].title", is(entry2.getTitle())));
    }

    @Test
    void getCalendarEntriesForGroup_invalidGroup_throwsException() throws Exception {
        // given
        Long invalidGroupId = 999L;
        String token = "valid-token";

        given(calendarEntryService.getCalendarEntriesForGroup(invalidGroupId, token))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/calendar-entries", invalidGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    private String asJsonString(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}