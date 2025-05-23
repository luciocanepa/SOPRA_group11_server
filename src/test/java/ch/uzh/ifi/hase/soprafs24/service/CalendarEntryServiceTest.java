package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.CalendarEntries;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.CalendarEntriesRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupMembershipRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class CalendarEntryServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CalendarEntriesRepository calendarEntriesRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMembershipRepository groupMembershipRepository;
    @Mock
    private UserService userService;
    @Mock
    private GroupService groupService;

    private CalendarEntryService calendarEntryService;

    private User testUser;
    private Group testGroup;
    private CalendarEntries testEntry;
    private GroupMembership testMembership;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Initialize test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("test-token");

        // Initialize test group
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setName("Test Group");

        // Initialize test calendar entry
        testEntry = new CalendarEntries();
        testEntry.setId(1L);
        testEntry.setTitle("Test Event");
        testEntry.setDescription("Test Description");
        testEntry.setStartTime(LocalDateTime.now().plusHours(1));
        testEntry.setEndTime(LocalDateTime.now().plusHours(2));
        testEntry.setGroup(testGroup);
        testEntry.setCreatedByUsername(testUser.getUsername());

        // Initialize test membership
        testMembership = new GroupMembership();
        testMembership.setUser(testUser);
        testMembership.setGroup(testGroup);

        // Initialize service with mocked dependencies
        calendarEntryService = new CalendarEntryService(
                calendarEntriesRepository,
                groupRepository,
                groupMembershipRepository,
                userService,
                groupService,
                userRepository);

        // Setup common mocks
        Mockito.when(userRepository.findByToken("test-token")).thenReturn(testUser);
        Mockito.when(groupService.findById(1L)).thenReturn(testGroup);
        Mockito.when(groupMembershipRepository.findByGroupAndUser(testGroup, testUser))
                .thenReturn(Optional.of(testMembership));
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(testUser);
    }

    @Test
    void createEntry_validInput_success() {
        // given
        CalendarEntries newEntry = new CalendarEntries();
        newEntry.setTitle("New Event");
        newEntry.setDescription("New Description");
        newEntry.setStartTime(LocalDateTime.now().plusHours(1));
        newEntry.setEndTime(LocalDateTime.now().plusHours(2));

        Mockito.when(calendarEntriesRepository.save(any(CalendarEntries.class))).thenReturn(testEntry);

        // when
        CalendarEntries createdEntry = calendarEntryService.createEntry(1L, newEntry, "test-token");

        // then
        assertNotNull(createdEntry);
        assertEquals(testEntry.getId(), createdEntry.getId());
        assertEquals(testEntry.getTitle(), createdEntry.getTitle());
        assertEquals(testEntry.getDescription(), createdEntry.getDescription());
        assertEquals(testEntry.getGroup(), createdEntry.getGroup());
        assertEquals(testUser.getUsername(), createdEntry.getCreatedByUsername());
    }

    @Test
    void createEntry_invalidGroup_throwsException() {
        // given
        Long invalidGroupId = 999L;
        Mockito.when(groupService.findById(invalidGroupId)).thenReturn(null);

        // when/then
        assertThrows(ResponseStatusException.class,
                () -> calendarEntryService.createEntry(invalidGroupId, testEntry, "test-token"));
    }

    @Test
    void createEntry_unauthorizedUser_throwsException() {
        // given
        Mockito.when(groupMembershipRepository.findByGroupAndUser(testGroup, testUser))
                .thenReturn(Optional.empty());

        // when/then
        assertThrows(ResponseStatusException.class,
                () -> calendarEntryService.createEntry(1L, testEntry, "test-token"));
    }

    @Test
    void getCalendarEntriesForGroup_validInput_success() {
        // given
        CalendarEntries futureEntry1 = new CalendarEntries();
        futureEntry1.setStartTime(LocalDateTime.now().plusHours(1));
        CalendarEntries futureEntry2 = new CalendarEntries();
        futureEntry2.setStartTime(LocalDateTime.now().plusHours(2));

        List<CalendarEntries> entries = Arrays.asList(futureEntry1, futureEntry2);
        Mockito.when(calendarEntriesRepository.findByGroupId(1L)).thenReturn(entries);

        // when
        List<CalendarEntries> result = calendarEntryService.getCalendarEntriesForGroup(1L, "test-token");

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getStartTime().isBefore(result.get(1).getStartTime()));
    }

    @Test
    void getCalendarEntriesForGroup_invalidGroup_throwsException() {
        // given
        Long invalidGroupId = 999L;
        Mockito.when(groupService.findById(invalidGroupId)).thenReturn(null);

        // when/then
        assertThrows(ResponseStatusException.class,
                () -> calendarEntryService.getCalendarEntriesForGroup(invalidGroupId, "test-token"));
    }

    @Test
    void getCalendarEntriesForGroup_unauthorizedUser_throwsException() {
        // given
        Mockito.when(groupMembershipRepository.findByGroupAndUser(testGroup, testUser))
                .thenReturn(Optional.empty());

        // when/then
        assertThrows(ResponseStatusException.class,
                () -> calendarEntryService.getCalendarEntriesForGroup(1L, "test-token"));
    }

    @Test
    void findByToken_validToken_success() {
        // when
        User foundUser = calendarEntryService.findByToken("test-token");

        // then
        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
    }

    @Test
    void findByToken_invalidToken_throwsException() {
        // given
        String invalidToken = "invalid-token";
        Mockito.when(userRepository.findByToken(invalidToken)).thenReturn(null);

        // when/then
        assertThrows(ResponseStatusException.class, () -> calendarEntryService.findByToken(invalidToken));
    }

    @Test
    void findByUsername_validUsername_success() {
        // given
        String username = "testUser";
        Mockito.when(userRepository.findByUsername(username)).thenReturn(testUser);

        // when
        User foundUser = calendarEntryService.findByUsername(username);

        // then
        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(username, foundUser.getUsername());
    }

    @Test
    void findByUsername_invalidUsername_throwsException() {
        // given
        String invalidUsername = "nonexistentUser";
        Mockito.when(userRepository.findByUsername(invalidUsername)).thenReturn(null);

        // when/then
        assertThrows(ResponseStatusException.class, () -> calendarEntryService.findByUsername(invalidUsername));
    }
}