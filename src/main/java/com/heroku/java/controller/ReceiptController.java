package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Payment;
import com.heroku.java.model.Staff;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.CustomerRepository;
import com.heroku.java.repository.PaymentRepository;
import com.heroku.java.repository.StaffRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ReceiptController {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final StaffRepository staffRepository;

    @Autowired
    public ReceiptController(AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            PaymentRepository paymentRepository,
            StaffRepository staffRepository) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.staffRepository = staffRepository;
    }

    @GetMapping("/receipt")
    public String receiptPage(@RequestParam Long appointmentId,
            @RequestParam(defaultValue = "view-appointment") String source,
            HttpSession session,
            Model model) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty())
            return "redirect:/index";

        Appointment appointment = appointmentOpt.get();

        // Ownership check: customers may only view their own receipts.
        // Staff (ADMIN/BARBER) can view any receipt.
        Long custId = (Long) session.getAttribute("custId");
        boolean isStaff = session.getAttribute("staffId") != null;
        if (!isStaff && (custId == null || !custId.equals(appointment.getCustId()))) {
            return "redirect:/appointment-history";
        }

        model.addAttribute("appointment", appointment);
        model.addAttribute("source", source);

        customerRepository.findById(appointment.getCustId()).ifPresent(c -> model.addAttribute("customer", c));
        staffRepository.findById(appointment.getBarberId()).ifPresent(s -> model.addAttribute("barber", s));
        paymentRepository.findByAppointmentId(appointmentId).ifPresent(p -> model.addAttribute("payment", p));

        return "customer/receipt";
    }
}
