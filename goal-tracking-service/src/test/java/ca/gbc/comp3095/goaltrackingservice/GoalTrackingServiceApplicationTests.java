package ca.gbc.comp3095.goaltrackingservice;

import ca.gbc.comp3095.goaltrackingservice.model.Goal;
import ca.gbc.comp3095.goaltrackingservice.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Goal Tracking Service using TestContainers for MongoDB.
 * Replaces the basic contextLoads() test with robust integration checks.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers // 1. Activates TestContainers support
class GoalTrackingServiceApplicationTests { // Using your existing file name

	// 2. Define the MongoDB TestContainer instance
	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

	@Autowired
	private GoalRepository goalRepository;

	// You must also add the Goal model and GoalRepository interfaces to your project for this to compile.

	private static final String STUDENT_ID = "GBC-12345";

	/**
	 * Configures Spring Boot to connect to the dynamic URI provided by the TestContainer.
	 */
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		// This directs the Spring app to the random, ephemeral MongoDB container URL
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	/**
	 * Clears the repository before each test to ensure test isolation.
	 */
	@BeforeEach
	void setup() {
		goalRepository.deleteAll();
	}

	// --- Core CRUD Integration Tests ---

	@Test
	void shouldCreateAndRetrieveGoal() {
		// GIVEN
		Goal newGoal = new Goal(null, STUDENT_ID, "Read Daily", "Read 1 chapter of a book.",
				"Academic", "IN_PROGRESS", LocalDateTime.now(), null);

		// WHEN
		Goal savedGoal = goalRepository.save(newGoal);
		Optional<Goal> foundGoal = goalRepository.findById(savedGoal.getId());

		// THEN
		assertTrue(foundGoal.isPresent());
		assertEquals("Read Daily", foundGoal.get().getTitle());
	}

	@Test
	void shouldUpdateGoalStatusToCompleted() {
		// GIVEN
		Goal initialGoal = new Goal(null, STUDENT_ID, "Exercise", "Weekly workout plan",
				"Fitness", "IN_PROGRESS", LocalDateTime.now(), null);
		Goal savedGoal = goalRepository.save(initialGoal);

		// WHEN
		savedGoal.setStatus("COMPLETED");
		Goal updatedGoal = goalRepository.save(savedGoal);

		// THEN
		assertEquals("COMPLETED", updatedGoal.getStatus());
	}

	@Test
	void shouldDeleteGoal() {
		// GIVEN
		Goal goalToDelete = new Goal(null, STUDENT_ID, "Networking", "Attend event",
				"Community", "IN_PROGRESS", LocalDateTime.now(), null);
		Goal savedGoal = goalRepository.save(goalToDelete);

		// WHEN
		goalRepository.deleteById(savedGoal.getId());
		Optional<Goal> foundGoal = goalRepository.findById(savedGoal.getId());

		// THEN
		assertFalse(foundGoal.isPresent());
	}

	// --- Filtering Integration Tests ---

	@Test
	void shouldFilterGoalsByCategory() {
		// GIVEN
		Goal academicGoal = new Goal(null, STUDENT_ID, "Study JS", "Finish module 2",
				"Academic", "IN_PROGRESS", LocalDateTime.now(), null);
		Goal fitnessGoal = new Goal(null, STUDENT_ID, "Run 5k", "Hit target time",
				"Fitness", "IN_PROGRESS", LocalDateTime.now(), null);
		goalRepository.saveAll(List.of(academicGoal, fitnessGoal));

		// WHEN (Assumes a findByCategory method exists on the repository)
		List<Goal> academicGoals = goalRepository.findByCategory("Academic");

		// THEN
		assertEquals(1, academicGoals.size());
		assertEquals("Study JS", academicGoals.get(0).getTitle());
	}
}