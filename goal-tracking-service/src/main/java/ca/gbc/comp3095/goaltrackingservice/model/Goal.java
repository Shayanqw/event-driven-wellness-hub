package ca.gbc.comp3095.goaltrackingservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "goals")
public class Goal {
    @Id
    private String id;

    private String studentId;
    private String title;
    private String description;
    private String category;
    private String status; // e.g. ACTIVE, COMPLETED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime targetDate;
}
