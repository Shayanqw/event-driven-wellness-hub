package ca.gbc.comp3095.goaltrackingservice.service;

import ca.gbc.comp3095.goaltrackingservice.model.Goal;
import ca.gbc.comp3095.goaltrackingservice.model.GoalCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GoalEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(GoalEventProducer.class);
    private static final String TOPIC = "goal-completed-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public GoalEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishGoalCompletedEvent(Goal goal) {
        try {
            GoalCompletedEvent event = GoalCompletedEvent.builder()
                    .goalId(goal.getId())
                    .studentId(goal.getStudentId())
                    .goalTitle(goal.getTitle())
                    .category(goal.getCategory())
                    .completedAt(LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, goal.getId(), eventJson);
            logger.info("Published GoalCompletedEvent for goal: {}", goal.getId());
        } catch (Exception e) {
            logger.error("Failed to publish GoalCompletedEvent for goal: {}", goal.getId(), e);
        }
    }
}

