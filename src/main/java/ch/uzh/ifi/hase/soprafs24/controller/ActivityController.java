package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Activity;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ActivityGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ActivityPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ActivityAggregateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.ActivityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity Controller
 * This class is responsible for handling all REST request that are related to
 * user activities.
 * The controller will receive the request and delegate the execution to the
 * ActivityService and finally return the result.
 */
@RestController
public class ActivityController {

    private final ActivityService activityService;

    ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/users/{userId}/statistics")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ActivityGetDTO createActivity(@PathVariable Long userId, @RequestBody ActivityPostDTO activityPostDTO, @RequestHeader("Authorization") String token) {
        Activity activityInput = DTOMapper.INSTANCE.convertActivityPostDTOtoEntity(activityPostDTO);
        Activity createdActivity = activityService.createActivity(activityInput, userId, token);

        return DTOMapper.INSTANCE.convertEntityToActivityGetDTO(createdActivity);
    }

    /*
     * Get all activities for a user
     * Can be filtered by date range: 
     *      startDate and endDate -> returns all activities between startDate and endDate
     *      startDate only -> returns all activities from startDate to today
     *      endDate only -> returns all activities from today to endDate
     * Can be aggregated by date
     *      aggregate = true -> returns the aggregated activities by date (total duration per day)
     */
    @GetMapping("/users/{userId}/statistics")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object getActivitiesByUserId(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "false") boolean aggregate) {
        
        if (aggregate) {
            return activityService.getAggregatedActivities(userId, token, startDate, endDate);
        }
        
        List<Activity> activities = activityService.getActivitiesByDateRange(userId, token, startDate, endDate);
        List<ActivityGetDTO> activityGetDTOs = new ArrayList<>();
        for (Activity activity : activities) {
            activityGetDTOs.add(DTOMapper.INSTANCE.convertEntityToActivityGetDTO(activity));
        }
        return activityGetDTOs;
    }
} 