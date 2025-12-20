package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Staff;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.CustomerRepository;
import com.heroku.java.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AdminController(CustomerRepository customerRepository,
                           StaffRepository staffRepository,
                           AppointmentRepository appointmentRepository) {
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/listCustomer")
    public String listCustomers(Model model) {
        model.addAttribute("customerList", customerRepository.findAll());
        return "admin/listCustomer";
    }

    @GetMapping("/listBarber")
    public String listBarbers(Model model) {
        // Fetch only staffs (assuming all staffs in repo are barbers for now)
        model.addAttribute("barberList", staffRepository.findAll());
        return "admin/listBarber";
    }

    @GetMapping("/listAppointment")
    public String listAppointments(Model model) {
        Iterable<Appointment> appointments = appointmentRepository.findAll();
        for (Appointment a : appointments) {
            customerRepository.findById(a.getCustId()).ifPresent(c -> a.setCustomerName(c.getCustName()));
        }
        model.addAttribute("appointmentList", appointments);
        return "admin/listAppointment";
    }
}
