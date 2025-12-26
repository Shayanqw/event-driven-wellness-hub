package ca.gbc.comp3095.eventservice.consumer;

import ca.gbc.comp3095.eventservice.model.Event;
import ca.gbc.comp3095.eventservice.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GoalCompletedEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GoalCompletedEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventRepository eventRepository;

    public GoalCompletedEventConsumer(ObjectMapper objectMapper, EventRepository eventRepository) {
        this.objectMapper = objectMapper;
        this.eventRepository = eventRepository;
    }

    @KafkaListener(topics = "goal-completed-events", groupId = "event-service-group")
    public void consumeGoalCompletedEvent(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = (Map<String, Object>) objectMapper.readValue(message, Map.class);
            logger.info("Received GoalCompletedEvent: {}", event);
            
            String category = (String) event.get("category");
            String goalTitle = (String) event.get("goalTitle");
            String studentId = (String) event.get("studentId");
            
            // Logic to recommend relevant wellness events based on completed goal
            logger.info("Goal '{}' completed in category '{}' by student '{}'. Recommending relevant wellness events.", 
                goalTitle, category, studentId);
            
            // Find upcoming events (events starting from now onwards)
            OffsetDateTime now = OffsetDateTime.now();
            List<Event> upcomingEvents = eventRepository.findByStartsAtBetween(now, now.plusMonths(3));
            
            // Filter events based on goal category keywords
            List<Event> recommendedEvents = recommendEventsByCategory(upcomingEvents, category);
            
            if (recommendedEvents.isEmpty()) {
                logger.info("No specific events found for category '{}'. Recommending all upcoming events.", category);
                recommendedEvents = upcomingEvents.stream()
                    .limit(5) // Limit to top 5 upcoming events
                    .collect(Collectors.toList());
            }
            
            // Log recommendations
            if (!recommendedEvents.isEmpty()) {
                logger.info("Recommended {} event(s) for completed goal '{}' in category '{}':", 
                    recommendedEvents.size(), goalTitle, category);
                recommendedEvents.forEach(eventRec -> 
                    logger.info("  - Event: '{}' at {} in {}", 
                        eventRec.getTitle(), 
                        eventRec.getStartsAt(), 
                        eventRec.getLocation() != null ? eventRec.getLocation() : "TBD")
                );
            } else {
                logger.info("No upcoming events available to recommend for category '{}'.", category);
            }
            
        } catch (Exception e) {
            logger.error("Error processing GoalCompletedEvent: {}", message, e);
        }
    }
    
    /**
     * Recommends events based on goal category by matching keywords in event titles.
     * 
     * @param events List of upcoming events
     * @param category Goal category (e.g., "Fitness", "Academic", "Mental Health")
     * @return List of recommended events matching the category
     */
    private List<Event> recommendEventsByCategory(List<Event> events, String category) {
        if (category == null || category.isEmpty()) {
            return events.stream().limit(5).collect(Collectors.toList());
        }
        
        String categoryLower = category.toLowerCase();
        
        // Map goal categories to event title keywords
        List<String> keywords = getKeywordsForCategory(categoryLower);
        
        return events.stream()
            .filter(event -> {
                String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
                return keywords.stream().anyMatch(title::contains);
            })
            .limit(5) // Limit recommendations to top 5
            .collect(Collectors.toList());
    }
    
    /**
     * Maps goal categories to relevant event title keywords.
     */
    private List<String> getKeywordsForCategory(String category) {
        String cat = category.toLowerCase();
        
        if (cat.contains("fitness") || cat.contains("exercise") || cat.contains("physical")) {
            return List.of("fitness", "workout", "exercise", "yoga", "running", "gym", "sports", "health");
        } else if (cat.contains("academic") || cat.contains("study") || cat.contains("learning")) {
            return List.of("study", "academic", "workshop", "seminar", "tutoring", "learning", "education");
        } else if (cat.contains("mental") || cat.contains("wellness") || cat.contains("stress")) {
            return List.of("wellness", "mental", "meditation", "mindfulness", "stress", "counseling", "support");
        } else if (cat.contains("nutrition") || cat.contains("diet") || cat.contains("food")) {
            return List.of("nutrition", "cooking", "diet", "healthy", "food", "meal");
        } else {
            // Default: return category itself and common wellness keywords
            return List.of(category, "wellness", "health", "event");
        }
    }
}

