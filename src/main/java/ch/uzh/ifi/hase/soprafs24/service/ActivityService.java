package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Activity;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.ActivityRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ActivityAggregateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    private static final String UNAUTHORIZED = "Invalid token";
    private static final String FORBIDDEN = "User is not authorized to perform this action";
    private static final String NOT_FOUND = "%s with ID %s was not found";
    private static final String INVALID_DATES = "Start date must be before or equal to end date";

    @Autowired
    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public Activity createActivity(Activity newActivity, Long userId, String token) {
        User authenticatedUser = validateTokenAndGetUser(token);

        if (!authenticatedUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN);
        }

        newActivity.setUser(authenticatedUser);

        newActivity = activityRepository.save(newActivity);
        activityRepository.flush();
        return newActivity;
    }

    public List<Activity> getActivitiesByDateRange(Long userId, String token, LocalDate startDate, LocalDate endDate) {
        User authenticatedUser = validateTokenAndGetUser(token);
        if (!authenticatedUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN);
        }

        // If no dates were provided, return all activities
        if (startDate == null && endDate == null) {
            return activityRepository.findByUserId(userId);
        }

        // Handle different date range scenarios
        LocalDateTime effectiveStartDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime effectiveEndDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : LocalDateTime.now();

        // validate date range
        if (effectiveStartDateTime.isAfter(effectiveEndDateTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_DATES);
        }

        return activityRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(
            authenticatedUser, effectiveStartDateTime, effectiveEndDateTime);
    }

    public List<ActivityAggregateDTO> getAggregatedActivities(Long userId, String token, LocalDate startDate, LocalDate endDate) {
        // Get the activities first & checks if the user is authorized
        List<Activity> activities = getActivitiesByDateRange(userId, token, startDate, endDate);

        Map<LocalDate, List<Activity>> groupedByDate = activities.stream()
            .collect(Collectors.groupingBy(activity -> activity.getStartDateTime().toLocalDate()));

        List<ActivityAggregateDTO> aggregatedResults = new ArrayList<>();
        groupedByDate.forEach((date, dailyActivities) -> {
            ActivityAggregateDTO aggregateDTO = new ActivityAggregateDTO();
            aggregateDTO.setDate(date);

            // Calculate total duration in minutes for all activities on this date
            long duration = dailyActivities.stream()
                    .mapToLong(activity -> 
                        Duration.between(activity.getStartDateTime(), activity.getEndDateTime()).toMinutes())
                    .sum();

            aggregateDTO.setDuration(duration);
            aggregatedResults.add(aggregateDTO);
        });

        // Sort by date
        aggregatedResults.sort((a1, a2) -> a1.getDate().compareTo(a2.getDate()));

        return aggregatedResults;
    }

    private User validateTokenAndGetUser(String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED);
        }
        return user;
    }
} 