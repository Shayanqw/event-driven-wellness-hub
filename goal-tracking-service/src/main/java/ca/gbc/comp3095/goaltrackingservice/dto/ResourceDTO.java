package ca.gbc.comp3095.goaltrackingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceDTO {
    private Long id;            // resource ID from wellness-resource-service
    private String title;       // resource title
    private String description; // short description of the resource
    private String category;    // category, e.g., Academic, Fitness, Mental Health
    private String url;         // link to the resource
}
