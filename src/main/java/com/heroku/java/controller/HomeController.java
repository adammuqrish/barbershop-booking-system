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

@Controller
public class HomeController {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public HomeController(PaymentRepository paymentRepository, 
                          CustomerRepository customerRepository, 
                          AppointmentRepository appointmentRepository) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    @GetMapping("/adminIndex")
    public String adminIndex(Model model) {
        model.addAttribute("totalSales", paymentRepository.getTotalSales() != null ? paymentRepository.getTotalSales() : 0.0);
        model.addAttribute("customerCount", customerRepository.count());
        model.addAttribute("totalAppointments", appointmentRepository.count());
        model.addAttribute("customerList", customerRepository.findAll());
        
        // Mock sales by day for now
        Map<String, Double> salesByDay = new HashMap<>();
        salesByDay.put("SUNDAY", 0.0);
        salesByDay.put("MONDAY", 0.0);
        salesByDay.put("TUESDAY", 0.0);
        salesByDay.put("WEDNESDAY", 0.0);
        salesByDay.put("THURSDAY", 0.0);
        salesByDay.put("FRIDAY", 0.0);
        salesByDay.put("SATURDAY", 0.0);
        model.addAttribute("salesByDay", salesByDay);

        return "admin/adminIndex";
    }
}
