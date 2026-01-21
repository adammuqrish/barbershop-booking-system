package com.heroku.java.repository;

import com.heroku.java.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByCustId(Long custId);
    Page<Appointment> findByCustId(Long custId, Pageable pageable);
    List<Appointment> findByCustIdAndServiceStatusIn(Long custId, List<String> statuses);
    Page<Appointment> findByCustIdAndServiceStatusIn(Long custId, List<String> statuses, Pageable pageable);

    List<Appointment> findByAppointmentDateAndAppointmentTime(String date, String time);
    List<Appointment> findByBarberIdAndAppointmentDateAndAppointmentTime(Long barberId, String date, String time);
    List<Appointment> findByBarberIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(Long barberId, String date, String time, Long appointmentId);

    @Query("SELECT a FROM Appointment a " +
           "ORDER BY a.appointmentDate DESC, " +
           "a.appointmentTime DESC, " +
           "CASE WHEN a.serviceStatus = 'pending' THEN 0 ELSE 1 END, " +
           "a.appointmentId DESC")
    List<Appointment> findAllSortedByDateAndStatus();

    List<Appointment> findByBarberIdAndAppointmentDate(Long barberId, String date);
    List<Appointment> findByAppointmentDate(String date);

    // ✅ Method ini ok, tak perlu @Query
    List<Appointment> findByBarberId(Long barberId);

    // ✅ Method ini ok, tak perlu @Query
    long countByBarberId(Long barberId);

    // ✅ WAJIB TAMBAH @Query untuk DISTINCT
    @Query("SELECT COUNT(DISTINCT a.custId) FROM Appointment a WHERE a.barberId = :staffId")
    long countDistinctCustomersByStaffId(@Param("staffId") Long staffId);
}