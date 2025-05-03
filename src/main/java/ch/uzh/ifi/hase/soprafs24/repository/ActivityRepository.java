package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Activity;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository("activityRepository")
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserId(Long userId);
    
    List<Activity> findByUserAndDateBetweenOrderByDateAsc(User user, LocalDate startDate, LocalDate endDate);
}