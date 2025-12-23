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

    @GetMapping("/adminIndex")
    public String adminIndex(Model model) {
        // Load staff from authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<com.heroku.java.model.Staff> staffOpt = staffRepository.findByStaffEmail(email);
            if (staffOpt.isPresent()) {
                com.heroku.java.model.Staff staff = staffOpt.get();
                model.addAttribute("staffName", staff.getStaffName());
                model.addAttribute("staffRole", staff.getStaffRole());
                model.addAttribute("staff", staff);
            } else {
                model.addAttribute("staffName", "Staff");
                model.addAttribute("staffRole", null);
            }
        } else {
            model.addAttribute("staffName", "Staff");
            model.addAttribute("staffRole", null);
        }

        // Get total sales as BigDecimal
        java.math.BigDecimal totalSales = paymentRepository.getTotalSales();
        if (totalSales == null) {
            totalSales = java.math.BigDecimal.ZERO;
        }
        model.addAttribute("totalSales", totalSales);
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
