package ca.gbc.comp3095.goaltrackingservice.controller;

import ca.gbc.comp3095.goaltrackingservice.client.ResourceClient;
import ca.gbc.comp3095.goaltrackingservice.dto.ResourceDTO;
import ca.gbc.comp3095.goaltrackingservice.model.Goal;
import ca.gbc.comp3095.goaltrackingservice.repository.GoalRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "*")
@Tag(name = "Goals", description = "API for managing student wellness goals")
public class GoalController {

    private final GoalRepository repo;
    private final ResourceClient resourceClient;
    private final ca.gbc.comp3095.goaltrackingservice.service.GoalEventProducer eventProducer;

    public GoalController(GoalRepository repo, ResourceClient resourceClient,
                         ca.gbc.comp3095.goaltrackingservice.service.GoalEventProducer eventProducer) {
        this.repo = repo;
        this.resourceClient = resourceClient;
        this.eventProducer = eventProducer;
    }

    // Create new goal
    @Operation(summary = "Create a new goal", description = "Create a new wellness goal (requires STUDENT role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Goal created successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - STUDENT role required")
    })
    @PostMapping
    public Goal createGoal(@RequestBody Goal goal) {
        goal.setCreatedAt(LocalDateTime.now());
        return repo.save(goal);
    }

    // Get all goals (with optional filters)
    @GetMapping
    public List<Goal> getGoals(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String category) {
        if (status != null) return repo.findByStatus(status);
        if (category != null) return repo.findByCategory(category);
        return repo.findAll();
    }

    // Get single goal by ID
    @GetMapping("/{id}")
    public Optional<Goal> getGoalById(@PathVariable String id) {
        return repo.findById(id);
    }

    // Update existing goal
    @Operation(summary = "Update a goal", description = "Update an existing goal. Publishes GoalCompletedEvent when status changes to COMPLETED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Goal updated successfully"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @PutMapping("/{id}")
    public Goal updateGoal(@PathVariable String id, @RequestBody Goal updatedGoal) {
        return repo.findById(id)
                .map(goal -> {
                    String oldStatus = goal.getStatus();
                    goal.setTitle(updatedGoal.getTitle());
                    goal.setDescription(updatedGoal.getDescription());
                    goal.setCategory(updatedGoal.getCategory());
                    goal.setStatus(updatedGoal.getStatus());
                    goal.setTargetDate(updatedGoal.getTargetDate());
                    Goal saved = repo.save(goal);
                    
                    // Publish event when goal is completed
                    if ("COMPLETED".equals(updatedGoal.getStatus()) && !"COMPLETED".equals(oldStatus)) {
                        eventProducer.publishGoalCompletedEvent(saved);
                    }
                    
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Goal not found with ID: " + id));
    }

    // Delete goal
    @DeleteMapping("/{id}")
    public String deleteGoal(@PathVariable String id) {
        repo.deleteById(id);
        return "Goal deleted successfully";
    }

    // NEW FEATURE: Get wellness resources related to a goal’s category
    @GetMapping("/{id}/resources")
    public ResponseEntity<?> getResourcesForGoal(@PathVariable String id) {
        Optional<Goal> optionalGoal = repo.findById(id);
        if (optionalGoal.isEmpty()) {
            return ResponseEntity.status(404).body("Goal not found with ID: " + id);
        }

        Goal goal = optionalGoal.get();
        if (goal.getCategory() == null || goal.getCategory().isBlank()) {
            return ResponseEntity.badRequest().body("Goal has no category");
        }

        List<ResourceDTO> resources = resourceClient.getResourcesByCategory(goal.getCategory());
        return ResponseEntity.ok(resources);
    }
}
