package ca.gbc.comp3095.wellnessresourceservice.repository;

import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WellnessResourceRepository extends JpaRepository<WellnessResource, Long> {

    // Spring Data JPA Query Method for filtering
    List<WellnessResource> findByCategory(String category);
}