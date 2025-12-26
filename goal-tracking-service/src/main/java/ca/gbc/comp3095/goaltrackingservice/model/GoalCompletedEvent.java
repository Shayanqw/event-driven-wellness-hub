package ca.gbc.comp3095.goaltrackingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalCompletedEvent {
    private String goalId;
    private String studentId;
    private String goalTitle;
    private String category;
    private LocalDateTime completedAt;
}

