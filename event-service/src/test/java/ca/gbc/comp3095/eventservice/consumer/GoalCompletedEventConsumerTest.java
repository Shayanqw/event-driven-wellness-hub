package ca.gbc.comp3095.eventservice.consumer;

import ca.gbc.comp3095.eventservice.model.Event;
import ca.gbc.comp3095.eventservice.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GoalCompletedEventConsumer.
 * Tests event recommendation logic based on goal categories.
 */
@ExtendWith(MockitoExtension.class)
class GoalCompletedEventConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private GoalCompletedEventConsumer consumer;

    private List<Event> mockEvents;

    @BeforeEach
    void setUp() {
        mockEvents = new ArrayList<>();
        mockEvents.add(new Event("Fitness Workshop"));
        mockEvents.add(new Event("Study Group Session"));
        mockEvents.add(new Event("Mental Health Support"));
    }

    @Test
    void shouldRecommendFitnessEventsForFitnessGoal() throws Exception {
        // GIVEN
        String eventJson = "{\"category\":\"Fitness\",\"goalTitle\":\"Run 5K\",\"studentId\":\"stu123\"}";

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("category", "Fitness");
        eventMap.put("goalTitle", "Run 5K");
        eventMap.put("studentId", "stu123");

        when(objectMapper.readValue(anyString(), any(Class.class)))
                .thenReturn(eventMap);
        when(eventRepository.findByStartsAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(mockEvents);

        // WHEN
        consumer.consumeGoalCompletedEvent(eventJson);

        // THEN
        verify(eventRepository, times(1))
                .findByStartsAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class));
    }

    @Test
    void shouldHandleInvalidEventJson() throws Exception {
        // GIVEN
        String invalidJson = "invalid json";

        when(objectMapper.readValue(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("Invalid JSON"));

        // WHEN
        consumer.consumeGoalCompletedEvent(invalidJson);

        // THEN - Should not throw exception, should log error
        verify(eventRepository, never())
                .findByStartsAtBetween(any(), any());
    }

    @Test
    void shouldRecommendEventsForAcademicGoal() throws Exception {
        // GIVEN
        String eventJson = "{\"category\":\"Academic\",\"goalTitle\":\"Complete Assignment\",\"studentId\":\"stu123\"}";

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("category", "Academic");
        eventMap.put("goalTitle", "Complete Assignment");
        eventMap.put("studentId", "stu123");

        when(objectMapper.readValue(anyString(), any(Class.class)))
                .thenReturn(eventMap);
        when(eventRepository.findByStartsAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(mockEvents);

        // WHEN
        consumer.consumeGoalCompletedEvent(eventJson);

        // THEN
        verify(eventRepository, times(1))
                .findByStartsAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class));
    }
}
