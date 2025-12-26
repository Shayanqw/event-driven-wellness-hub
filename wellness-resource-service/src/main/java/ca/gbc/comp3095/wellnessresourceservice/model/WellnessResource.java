package ca.gbc.comp3095.wellnessresourceservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellnessResource implements Serializable {
    // Recommended: Add a serial version UID
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String category;
    private String url;
}
