package com.heroku.java.service;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.Staff;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final AppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;

    @Autowired
    public BookingService(AppointmentRepository appointmentRepository, StaffRepository staffRepository) {
        this.appointmentRepository = appointmentRepository;
        this.staffRepository = staffRepository;
    }

    public List<Staff> getAllBarbers() {
        return (List<Staff>) staffRepository.findAll();
    }

    public Map<String, List<Long>> getUnavailableBarbersBySlot(String date, String[] slots) {
        return List.of(slots).stream().collect(Collectors.toMap(
                slot -> slot,
                slot -> appointmentRepository.findByAppointmentDateAndAppointmentTime(date, slot)
                        .stream()
                        .map(Appointment::getBarberId)
                        .distinct()
                        .collect(Collectors.toList())));
    }

    public boolean isBarberAvailable(Long barberId, String date, String time) {
        return appointmentRepository.findByBarberIdAndAppointmentDateAndAppointmentTime(barberId, date, time).isEmpty();
    }

    public boolean isBarberAvailableForUpdate(Long barberId, String date, String time, Long excludeAppointmentId) {
        return appointmentRepository.findByBarberIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(barberId,
                date, time, excludeAppointmentId).isEmpty();
    }

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
}
