package com.heroku.java.repository;

import com.heroku.java.model.Appointment;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends CrudRepository<Appointment, Long> {
    List<Appointment> findByCustId(Long custId);
    List<Appointment> findByAppointmentDateAndAppointmentTime(String date, String time);
    List<Appointment> findByBarberIdAndAppointmentDateAndAppointmentTime(Long barberId, String date, String time);
}
