package com.heroku.java.repository;

import com.heroku.java.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Appointment> findByBarberIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(Long barberId, String date,
            String time, Long appointmentId);
}
