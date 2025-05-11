package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.CalendarEntries;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CalendarEntriesGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CalendarEntriesPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.CalendarEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CalendarEntriesController {

  private final CalendarEntryService calendarEntryService;

  public CalendarEntriesController(CalendarEntryService calendarEntryService) {
    this.calendarEntryService = calendarEntryService;
  }

  @PostMapping("/calendar-entries/groups/{groupId}")
  @ResponseStatus(HttpStatus.CREATED)
  public CalendarEntriesGetDTO createCalendarEntry(@PathVariable Long groupId, @RequestBody CalendarEntriesPostDTO postDTO, @RequestHeader("Authorization") String token) {

    CalendarEntries entryInput = DTOMapper.INSTANCE.convertCalendarEntryPostDTOtoEntity(postDTO);
    CalendarEntries created = calendarEntryService.createEntry(groupId, entryInput, token);

    return DTOMapper.INSTANCE.convertEntityToCalendarEntryGetDTO(created);
  }

  
  @GetMapping("/calendar-entries/groups/{groupId}")
  @ResponseStatus(HttpStatus.OK)
  public List<CalendarEntriesGetDTO> getCalendarEntriesForGroup(@PathVariable Long groupId, @RequestHeader("Authorization") String token) {
    List<CalendarEntries> entries = calendarEntryService.getCalendarEntriesForGroup(groupId, token);
    return entries.stream()
            .map(DTOMapper.INSTANCE::convertEntityToCalendarEntryGetDTO)
            .collect(Collectors.toList());
  }

}
