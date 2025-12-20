package com.heroku.java.repository;

import com.heroku.java.model.Payment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
    Optional<Payment> findByAppointmentId(Long appointmentId);

    @Query("SELECT SUM(p.amount) FROM Payment p")
    Double getTotalSales();
}
