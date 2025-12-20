package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.StaffRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;

    @Autowired
    public AppointmentController(AppointmentRepository appointmentRepository, StaffRepository staffRepository) {
        this.appointmentRepository = appointmentRepository;
        this.staffRepository = staffRepository;
    }

    @GetMapping("/view-appointment")
    public String viewAppointments(HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null) return "redirect:/register";

        List<Appointment> appointments = appointmentRepository.findByCustId(custId);
        model.addAttribute("appointments", appointments);
        
        // Populate barber name for each appointment (could be optimized with Join in repo)
        for (Appointment appt : appointments) {
             staffRepository.findById(appt.getBarberId()).ifPresent(s -> appt.setAppointmentBarber(s.getStaffName()));
        }

        return "customer/view-appointment";
    }

    @GetMapping("/appointment-history")
    public String appointmentHistory(HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null) return "redirect:/register";

        // For now, same as view-appointment but could filter by status
        List<Appointment> appointments = appointmentRepository.findByCustId(custId);
        model.addAttribute("appointments", appointments);

        return "customer/appointment-history";
    }

    @PostMapping("/cancel-appointment")
    public String cancelAppointment(@RequestParam Long appointmentId, HttpSession session) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null) return "redirect:/register";

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            if (appointment.getCustId().equals(custId)) {
                appointment.setServiceStatus("cancelled");
                appointmentRepository.save(appointment);
            }
        }

        return "redirect:/view-appointment";
    }
}