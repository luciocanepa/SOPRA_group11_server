package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Activity;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.repository.ActivityRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ActivityAggregateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserActivitiesGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserAggregatedActivitiesGetDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private ActivityService activityService;

    private User testUser;
    private User testUser2;
    private Group testGroup;
    private Activity testActivity1;
    private Activity testActivity2;
    private Activity testActivity3;
    private String validToken = "valid-token";
    private GroupMembership testMembership;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setToken(validToken);
        testUser.setUsername("testUser");
        testUser.setName("Test User");

        // Create second test user
        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setToken("user2-token");
        testUser2.setUsername("testUser2");
        testUser2.setName("Test User 2");

        // Create test group
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setName("Test Group");

        // Create test membership
        testMembership = new GroupMembership();
        testMembership.setUser(testUser);
        testMembership.setGroup(testGroup);
        testMembership.setStatus(MembershipStatus.ACTIVE);

        // Create test activities
        testActivity1 = new Activity();
        testActivity1.setId(1L);
        testActivity1.setUser(testUser);
        testActivity1.setStartDateTime(LocalDate.of(2024, 3, 1).atTime(9, 0));
        testActivity1.setEndDateTime(LocalDate.of(2024, 3, 1).atTime(10, 0));

        testActivity2 = new Activity();
        testActivity2.setId(2L);
        testActivity2.setUser(testUser);
        testActivity2.setStartDateTime(LocalDate.of(2024, 3, 1).atTime(14, 0));
        testActivity2.setEndDateTime(LocalDate.of(2024, 3, 1).atTime(15, 0));

        testActivity3 = new Activity();
        testActivity3.setId(3L);
        testActivity3.setUser(testUser);
        testActivity3.setStartDateTime(LocalDate.of(2024, 3, 15).atTime(11, 0));
        testActivity3.setEndDateTime(LocalDate.of(2024, 3, 15).atTime(12, 30));

        // Mock userRepository behavior for both users
        when(userRepository.findByToken(validToken)).thenReturn(testUser);
        when(userRepository.findByToken(testUser2.getToken())).thenReturn(testUser2);
    }

    @Test
    void getActivitiesByDateRange_validDates_success() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        
        when(activityRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(any(), any(), any()))
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
        
        when(activityRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(any(), any(), any()))
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
        
        when(activityRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(any(), any(), any()))
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

    @Test
    void createActivity_success() {
        // given
        Activity newActivity = new Activity();
        newActivity.setStartDateTime(LocalDate.now().atTime(10, 0));
        newActivity.setEndDateTime(LocalDate.now().atTime(11, 0));

        when(activityRepository.save(any(Activity.class))).thenReturn(newActivity);

        // when
        Activity createdActivity = activityService.createActivity(newActivity, testUser.getId(), validToken);

        // then
        assertNotNull(createdActivity);
        assertEquals(testUser, createdActivity.getUser());
    }

    @Test
    void createActivity_invalidToken_throwsException() {
        // given
        Activity newActivity = new Activity();
        String invalidToken = "invalid-token";

        when(userRepository.findByToken(invalidToken)).thenReturn(null);

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            activityService.createActivity(newActivity, testUser.getId(), invalidToken);
        });
    }

    @Test
    void createActivity_unauthorizedUser_throwsException() {
        // given
        Activity newActivity = new Activity();
        Long unauthorizedUserId = 2L;

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            activityService.createActivity(newActivity, unauthorizedUserId, validToken);
        });
    }

    @Test
    void getGroupUsersActivities_success() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        when(groupRepository.findById(testGroup.getId())).thenReturn(Optional.of(testGroup));
        when(membershipService.findByUserAndGroup(testUser, testGroup)).thenReturn(testMembership);
        when(membershipService.getActiveUsersInGroup(testGroup)).thenReturn(Arrays.asList(testUser, testUser2));
        
        // Mock activities for both users
        when(activityRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(
            eq(testUser), any(), any())).thenReturn(Arrays.asList(testActivity1, testActivity2));
        
        Activity testUser2Activity = new Activity();
        testUser2Activity.setId(4L);
        testUser2Activity.setUser(testUser2);
        testUser2Activity.setStartDateTime(LocalDate.of(2024, 3, 1).atTime(10, 0));
        testUser2Activity.setEndDateTime(LocalDate.of(2024, 3, 1).atTime(11, 0));
        
        when(activityRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(
            eq(testUser2), any(), any())).thenReturn(Collections.singletonList(testUser2Activity));

        // when
        List<UserActivitiesGetDTO> result = activityService.getGroupUsersActivities(
            testUser.getId(), validToken, testGroup.getId(), startDate, endDate);

        // then
        assertEquals(2, result.size());
        UserActivitiesGetDTO firstUserActivities = result.get(0);
        assertEquals(testUser.getId(), firstUserActivities.getUserId());
        assertEquals(2, firstUserActivities.getActivities().size());
        
        UserActivitiesGetDTO secondUserActivities = result.get(1);
        assertEquals(testUser2.getId(), secondUserActivities.getUserId());
        assertEquals(1, secondUserActivities.getActivities().size());
    }

    @Test
    void getGroupUsersActivities_groupNotFound_throwsException() {
        // given
        Long nonExistentGroupId = 999L;
        when(groupRepository.findById(nonExistentGroupId)).thenReturn(Optional.empty());

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            activityService.getGroupUsersActivities(
                testUser.getId(), validToken, nonExistentGroupId, null, null);
        });
    }

    @Test
    void getGroupUsersAggregatedActivities_success() {
        // given
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        when(groupRepository.findById(testGroup.getId())).thenReturn(Optional.of(testGroup));
        when(membershipService.findByUserAndGroup(testUser, testGroup)).thenReturn(testMembership);
        when(membershipService.getActiveUsersInGroup(testGroup)).thenReturn(Arrays.asList(testUser, testUser2));
        
        // Mock activities for both users
        when(activityRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(
            eq(testUser), any(), any())).thenReturn(Arrays.asList(testActivity1, testActivity2));
        
        Activity testUser2Activity = new Activity();
        testUser2Activity.setId(4L);
        testUser2Activity.setUser(testUser2);
        testUser2Activity.setStartDateTime(LocalDate.of(2024, 3, 1).atTime(10, 0));
        testUser2Activity.setEndDateTime(LocalDate.of(2024, 3, 1).atTime(11, 0));
        
        when(activityRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(
            eq(testUser2), any(), any())).thenReturn(Collections.singletonList(testUser2Activity));

        // when
        List<UserAggregatedActivitiesGetDTO> result = activityService.getGroupUsersAggregatedActivities(
            testUser.getId(), validToken, testGroup.getId(), startDate, endDate);

        // then
        assertEquals(2, result.size());
        UserAggregatedActivitiesGetDTO firstUserAggregated = result.get(0);
        assertEquals(testUser.getId(), firstUserAggregated.getUserId());
        assertNotNull(firstUserAggregated.getAggregatedActivities());
        assertEquals(1, firstUserAggregated.getAggregatedActivities().size()); // One day with activities
        
        UserAggregatedActivitiesGetDTO secondUserAggregated = result.get(1);
        assertEquals(testUser2.getId(), secondUserAggregated.getUserId());
        assertNotNull(secondUserAggregated.getAggregatedActivities());
        assertEquals(1, secondUserAggregated.getAggregatedActivities().size()); // One day with activities
    }

    @Test
    void getGroupUsersAggregatedActivities_notGroupMember_throwsException() {
        // given
        when(groupRepository.findById(testGroup.getId())).thenReturn(Optional.of(testGroup));
        
        GroupMembership inactiveMembership = new GroupMembership();
        inactiveMembership.setStatus(MembershipStatus.PENDING);
        when(membershipService.findByUserAndGroup(testUser, testGroup)).thenReturn(inactiveMembership);

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            activityService.getGroupUsersAggregatedActivities(
                testUser.getId(), validToken, testGroup.getId(), null, null);
        });
    }
} 