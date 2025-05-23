package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.CalendarEntries;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.CalendarEntriesRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupMembershipRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import ch.uzh.ifi.hase.soprafs24.entity.Group;

@Service
@Transactional
public class CalendarEntryService {
    private static final String UNAUTHORIZED = "Invalid token";
    private static final String NOT_FOUND = "%s with ID %s was not found";

    private final CalendarEntriesRepository calendarEntriesRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final GroupService groupService;
    private final UserRepository userRepository;

    public CalendarEntryService(
            CalendarEntriesRepository calendarEntriesRepository,
            GroupRepository groupRepository,
            GroupMembershipRepository groupMembershipRepository,
            UserService userService,
            GroupService groupService,
            UserRepository userRepository) {
        this.calendarEntriesRepository = calendarEntriesRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.groupService = groupService;
        this.userRepository = userRepository;
    }

    public User findByToken(String token) {
        User userByToken = userRepository.findByToken(token);

        if (userByToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED);
        }

        return userByToken;
    }

    public User findByUsername(String name) {
        User userByUsername = userRepository.findByUsername(name);

        if (userByUsername == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED);
        }

        return userByUsername;
    }

    public CalendarEntries createEntry(Long groupId, CalendarEntries request, String token) {
        User authenticatedUser = findByToken(token);

        Group group = groupService.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(NOT_FOUND, "Group", groupId));
        }

        if (groupMembershipRepository.findByGroupAndUser(group, authenticatedUser).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, UNAUTHORIZED);
        }

        request.setGroup(group);
        request.setCreatedByUsername(authenticatedUser.getUsername());

        return calendarEntriesRepository.save(request);
    }

    public List<CalendarEntries> getCalendarEntriesForGroup(Long groupId, String token) {
        User authenticatedUser = findByToken(token);

        Group group = groupService.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(NOT_FOUND, "Group", groupId));
        }

        if (groupMembershipRepository.findByGroupAndUser(group, authenticatedUser).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, UNAUTHORIZED);
        }

        LocalDateTime now = LocalDateTime.now();

        return calendarEntriesRepository.findByGroupId(groupId).stream()
                .filter(entry -> entry.getStartTime().isAfter(now))
                .sorted(Comparator.comparing(CalendarEntries::getStartTime))
                .collect(Collectors.toList());
    }
}
