package com.heroku.java.repository;

import com.heroku.java.model.Payment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
    Optional<Payment> findByAppointmentId(Long appointmentId);
    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT SUM(p.amount) FROM Payment p")
    java.math.BigDecimal getTotalSales();

    // ✅ TUKAR METHOD NI: Guna subquery supaya tak perlu JOIN objek Appointment
    // Kita cari Payment dimana appointment_id NYA ada dalam senarai appointment untuk staffId tu
    @Query("SELECT p FROM Payment p WHERE p.appointmentId IN " +
           "(SELECT a.appointmentId FROM Appointment a WHERE a.barberId = :staffId)")
    List<Payment> findAllByStaffId(@Param("staffId") Long staffId);

    // ✅ TUKAR METHOD NI: Sama macam atas, tapi SUM amount
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.appointmentId IN " +
           "(SELECT a.appointmentId FROM Appointment a WHERE a.barberId = :staffId)")
    BigDecimal getTotalSalesByStaffId(@Param("staffId") Long staffId);
}