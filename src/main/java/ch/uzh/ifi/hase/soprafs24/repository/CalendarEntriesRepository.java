package ch.uzh.ifi.hase.soprafs24.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs24.entity.CalendarEntries;
import java.util.List;

@Repository("calendarEntriesRepository")
public interface CalendarEntriesRepository extends JpaRepository<CalendarEntries, Long> {
    List<CalendarEntries> findByGroupId(Long groupId);
}
