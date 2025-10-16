package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AppointmentController {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@GetMapping("/appointments")
	public String listAppointments(Model model) {
		List<Appointment> appointments = appointmentRepository.findAll();
		System.out.println("Jumlah rekod ditemui: " + appointments.size());
		for (Appointment a : appointments) {
			System.out.println("Rekod: " + a.getAppointmentId() + " - " + a.getAppointmentDate());
		}
		model.addAttribute("appointments", appointments);
		return "list-appointments";
	}
}