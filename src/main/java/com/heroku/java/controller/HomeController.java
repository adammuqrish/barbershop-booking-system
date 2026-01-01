package com.heroku.java.controller;

import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.CustomerRepository;
import com.heroku.java.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class HomeController {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final com.heroku.java.repository.StaffRepository staffRepository;

    @Autowired
    public HomeController(PaymentRepository paymentRepository,
            CustomerRepository customerRepository,
            AppointmentRepository appointmentRepository,
            com.heroku.java.repository.StaffRepository staffRepository) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
        this.staffRepository = staffRepository;
    }

    @GetMapping({ "/", "/index" })
    public String index() {
        return "index";
    }
}