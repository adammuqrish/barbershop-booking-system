package com.heroku.java.repository;

import com.heroku.java.model.Feedback;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends CrudRepository<Feedback, Long> {
    Optional<Feedback> findByAppointmentId(Long appointmentId);

    // ✅ KEMASKINI METHOD INI
    // Guna Subquery: Cari feedback dimana appointmentId NYA ada dalam senarai appointment barber tu
    @Query("SELECT f FROM Feedback f WHERE f.appointmentId IN " +
           "(SELECT a.appointmentId FROM Appointment a WHERE a.barberId = :staffId)")
    List<Feedback> findByStaffId(@Param("staffId") Long staffId);
}