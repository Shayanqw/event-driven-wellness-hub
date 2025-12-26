package ca.gbc.comp3095.eventservice.repository;

import ca.gbc.comp3095.eventservice.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStartsAtBetween(OffsetDateTime start, OffsetDateTime end);

    List<Event> findByLocationContainingIgnoreCase(String q);

    List<Event> findByStartsAtBetweenAndLocationContainingIgnoreCase(
            OffsetDateTime start, OffsetDateTime end, String q);
}
