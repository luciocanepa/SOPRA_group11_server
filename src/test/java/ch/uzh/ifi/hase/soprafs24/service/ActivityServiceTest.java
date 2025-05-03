package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Activity;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.ActivityRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ActivityAggregateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivityService activityService;

    private User testUser;
    private Activity testActivity1;
    private Activity testActivity2;
    private Activity testActivity3;
    private String validToken = "valid-token";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setToken(validToken);

        // Create test activities
        testActivity1 = new Activity();
        testActivity1.setId(1L);
        testActivity1.setUser(testUser);
        testActivity1.setDate(LocalDate.of(2024, 3, 1));
        testActivity1.setStartTime(LocalTime.of(9, 0));
        testActivity1.setEndTime(LocalTime.of(10, 0));

        testActivity2 = new Activity();
        testActivity2.setId(2L);
        testActivity2.setUser(testUser);
        testActivity2.setDate(LocalDate.of(2024, 3, 1)); // Same date as activity1
        testActivity2.setStartTime(LocalTime.of(14, 0));
        testActivity2.setEndTime(LocalTime.of(15, 0));

        testActivity3 = new Activity();
        testActivity3.setId(3L);
        testActivity3.setUser(testUser);
        testActivity3.setDate(LocalDate.of(2024, 3, 15));
        testActivity3.setStartTime(LocalTime.of(11, 0));
        testActivity3.setEndTime(LocalTime.of(12, 30));

        // Mock userRepository behavior
        when(userRepository.findByToken(validToken)).thenReturn(testUser);
    }

    @Test
    void getActivitiesByDateRange_validDates_success() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        
        when(activityRepository.findByUserAndDateBetweenOrderByDateAsc(any(), any(), any()))
                .thenReturn(Arrays.asList(testActivity1, testActivity2));

        // when
        List<Activity> activities = activityService.getActivitiesByDateRange(testUser.getId(), validToken, startDate, endDate);

        // then
        assertEquals(2, activities.size());
        assertEquals(testActivity1.getId(), activities.get(0).getId());
        assertEquals(testActivity2.getId(), activities.get(1).getId());
    }

    @Test
    void getActivitiesByDateRange_invalidDateRange_throwsException() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 31);
        LocalDate endDate = LocalDate.of(2024, 3, 1);

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            activityService.getActivitiesByDateRange(testUser.getId(), validToken, startDate, endDate);
        });
    }

    @Test
    void getActivitiesByDateRange_invalidToken_throwsException() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        String invalidToken = "invalid-token";

        when(userRepository.findByToken(invalidToken)).thenReturn(null);

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            activityService.getActivitiesByDateRange(testUser.getId(), invalidToken, startDate, endDate);
        });
    }

    @Test
    void getActivitiesByDateRange_unauthorizedUser_throwsException() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        Long unauthorizedUserId = 2L;

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            activityService.getActivitiesByDateRange(unauthorizedUserId, validToken, startDate, endDate);
        });
    }

    @Test
    void getActivitiesByDateRange_nullDates_returnsAllActivities() {
        // given
        when(activityRepository.findByUserId(testUser.getId()))
                .thenReturn(Arrays.asList(testActivity1, testActivity2));

        // when
        List<Activity> activities = activityService.getActivitiesByDateRange(testUser.getId(), validToken, null, null);

        // then
        assertEquals(2, activities.size());
        assertEquals(testActivity1.getId(), activities.get(0).getId());
        assertEquals(testActivity2.getId(), activities.get(1).getId());
    }

    @Test
    void getAggregatedActivities_success() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        
        when(activityRepository.findByUserAndDateBetweenOrderByDateAsc(any(), any(), any()))
                .thenReturn(Arrays.asList(testActivity1, testActivity2, testActivity3));

        // when
        List<ActivityAggregateDTO> aggregatedActivities = 
            activityService.getAggregatedActivities(testUser.getId(), validToken, startDate, endDate);

        // then
        assertEquals(2, aggregatedActivities.size()); // Should have 2 dates

        // Check March 1st (two activities, total 120 minutes)
        ActivityAggregateDTO march1 = aggregatedActivities.get(0);
        assertEquals(LocalDate.of(2024, 3, 1), march1.getDate());
        assertEquals(120, march1.getDuration()); // 2 hours total (2 activities of 1 hour each)

        // Check March 15th (one activity, 90 minutes)
        ActivityAggregateDTO march15 = aggregatedActivities.get(1);
        assertEquals(LocalDate.of(2024, 3, 15), march15.getDate());
        assertEquals(90, march15.getDuration()); // 1.5 hours
    }

    @Test
    void getAggregatedActivities_noActivities_returnsEmptyList() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        
        when(activityRepository.findByUserAndDateBetweenOrderByDateAsc(any(), any(), any()))
                .thenReturn(List.of());

        // when
        List<ActivityAggregateDTO> aggregatedActivities = 
            activityService.getAggregatedActivities(testUser.getId(), validToken, startDate, endDate);

        // then
        assertTrue(aggregatedActivities.isEmpty());
    }

    @Test
    void getAggregatedActivities_invalidToken_throwsException() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        String invalidToken = "invalid-token";

        when(userRepository.findByToken(invalidToken)).thenReturn(null);

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            activityService.getAggregatedActivities(testUser.getId(), invalidToken, startDate, endDate);
        });
    }
} 