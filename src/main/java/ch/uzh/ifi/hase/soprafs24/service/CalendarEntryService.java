package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.CalendarEntries;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.CalendarEntriesRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import ch.uzh.ifi.hase.soprafs24.entity.Group;

@Service
public class CalendarEntryService {
    @Autowired
    private CalendarEntriesRepository repository;

    @Autowired
    private UserRepository userRepository;

    private static final String UNAUTHORIZED = "Invalid token";


    public User findByToken(String token) {
        User userByToken = userRepository.findByToken(token);

        if (userByToken == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED);
        }

        return userByToken;
    }

    public CalendarEntries createEntry(Long groupId, CalendarEntries request, String token) {
        CalendarEntries session = new CalendarEntries();

        User creator = findByToken(token);
        Group group = new Group();

        group.setId(groupId);
        session.setGroup(group);

        session.setCreatedByUsername(creator.getUsername());
        session.setTitle(request.getTitle());
        session.setDescription(request.getDescription());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());

        return repository.save(session);
    }

    public List<CalendarEntries> getCalendarEntriesForGroup(Long groupId, String token) {
            LocalDateTime now = LocalDateTime.now();

    return repository.findByGroupId(groupId).stream()
        .filter(entry -> entry.getStartTime().isAfter(now))
        .sorted(Comparator.comparing(CalendarEntries::getStartTime))
        .collect(Collectors.toList());
    }
}
