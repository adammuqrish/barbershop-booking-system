package com.heroku.java.repository;

import com.heroku.java.model.Feedback;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository; // Added import for @Repository
import java.util.Optional;

@Repository // Added @Repository annotation
public interface FeedbackRepository extends CrudRepository<Feedback, Long> {
    Optional<Feedback> findByAppointmentId(Long appointmentId);
}
