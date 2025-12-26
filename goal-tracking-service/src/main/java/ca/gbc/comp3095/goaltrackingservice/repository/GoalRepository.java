package ca.gbc.comp3095.goaltrackingservice.repository;

import ca.gbc.comp3095.goaltrackingservice.model.Goal;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends MongoRepository<Goal, String> {
    List<Goal> findByStatus(String status);
    List<Goal> findByCategory(String category);
}
