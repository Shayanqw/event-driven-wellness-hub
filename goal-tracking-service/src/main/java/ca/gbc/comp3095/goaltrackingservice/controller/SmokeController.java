package ca.gbc.comp3095.goaltrackingservice.controller;

import ca.gbc.comp3095.goaltrackingservice.model.Goal;
import ca.gbc.comp3095.goaltrackingservice.repository.GoalRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/quick")
@CrossOrigin(origins = "*") // allows testing from Postman or browser
public class SmokeController {

    private final GoalRepository repo;

    // Constructor injection (Spring automatically provides the repository)
    public SmokeController(GoalRepository repo) {
        this.repo = repo;
    }

    // Simple test endpoint
    @GetMapping("/test")
    public String test() {
        return "goal-tracking-service is running ✅";
    }

    // Create a sample goal in MongoDB
    @PostMapping("/create-sample")
    public Goal createSample() {
        Goal goal = Goal.builder()
                .studentId("stu1")
                .title("Sample Goal")
                .description("Created for smoke test")
                .category("test")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .targetDate(LocalDateTime.now().plusDays(7))
                .build();
        return repo.save(goal);
    }

    // Return all goals from MongoDB
    @GetMapping("/all")
    public List<Goal> getAll() {
        return repo.findAll();
    }
}
