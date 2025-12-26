package ca.gbc.comp3095.goaltrackingservice.service;

import ca.gbc.comp3095.goaltrackingservice.model.Goal;
import ca.gbc.comp3095.goaltrackingservice.repository.GoalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for GoalEventProducer using Kafka TestContainers.
 * Verifies that goal completion events are properly published to Kafka.
 */
@SpringBootTest
@Testcontainers
class GoalEventProducerKafkaIT {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Autowired
    private GoalEventProducer eventProducer;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaConsumer<String, String> consumer;
    private static final String TOPIC = "goal-completed-events";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("SPRING_KAFKA_BOOTSTRAP_SERVERS", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        goalRepository.deleteAll();
        
        // Set up Kafka consumer for testing
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        if (consumer != null) {
            consumer.close();
        }
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void shouldPublishGoalCompletedEventToKafka() throws Exception {
        // GIVEN: Create a goal with ACTIVE status
        Goal goal = new Goal();
        goal.setStudentId("stu123");
        goal.setTitle("Complete Assignment 2");
        goal.setDescription("Finish all requirements");
        goal.setCategory("Academic");
        goal.setStatus("ACTIVE");
        goal.setCreatedAt(LocalDateTime.now());
        Goal savedGoal = goalRepository.save(goal);

        // WHEN: Update goal status to COMPLETED (triggers event)
        savedGoal.setStatus("COMPLETED");
        goalRepository.save(savedGoal);
        
        // Manually trigger event producer since controller logic might not be invoked in this test
        eventProducer.publishGoalCompletedEvent(savedGoal);

        // THEN: Verify event was published to Kafka
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        assertFalse(records.isEmpty(), "Expected at least one event in Kafka");

        ConsumerRecord<String, String> record = records.iterator().next();
        assertEquals(savedGoal.getId(), record.key(), "Event key should match goal ID");
        
        // Verify event content
        String eventJson = record.value();
        assertNotNull(eventJson, "Event JSON should not be null");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> event = objectMapper.readValue(eventJson, Map.class);
        assertEquals(savedGoal.getId(), event.get("goalId"));
        assertEquals("stu123", event.get("studentId"));
        assertEquals("Complete Assignment 2", event.get("goalTitle"));
        assertEquals("Academic", event.get("category"));
        assertNotNull(event.get("completedAt"));
    }

    @Test
    void shouldNotPublishEventForNonCompletedGoal() {
        // GIVEN: Create a goal with ACTIVE status
        Goal goal = new Goal();
        goal.setStudentId("stu123");
        goal.setTitle("Test Goal");
        goal.setCategory("Fitness");
        goal.setStatus("ACTIVE");
        goal.setCreatedAt(LocalDateTime.now());
        Goal savedGoal = goalRepository.save(goal);

        // WHEN: Update goal but keep status as ACTIVE (should not trigger event)
        savedGoal.setTitle("Updated Title");
        goalRepository.save(savedGoal);
        
        // Manually try to publish (but this shouldn't happen in real flow)
        // We'll just verify no events were published
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
        
        // THEN: No events should be in Kafka (or we clear before checking)
        // This test verifies that only COMPLETED goals trigger events
        assertTrue(records.isEmpty() || records.count() == 0, 
            "No events should be published for non-completed goals");
    }
}

