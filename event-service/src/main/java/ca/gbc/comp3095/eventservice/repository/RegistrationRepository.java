package ca.gbc.comp3095.eventservice.repository;

import ca.gbc.comp3095.eventservice.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByEvent_Id(Long eventId);
}
