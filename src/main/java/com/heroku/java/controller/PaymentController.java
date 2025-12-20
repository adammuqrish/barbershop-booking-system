package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.CashPayment;
import com.heroku.java.model.OnlinePayment;
import com.heroku.java.model.Payment;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.PaymentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Optional;

@Controller
public class PaymentController {

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentController(AppointmentRepository appointmentRepository, PaymentRepository paymentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/payment")
    public String paymentPage(HttpSession session, Model model) {
        Long appointmentId = (Long) session.getAttribute("lastAppointmentId");
        if (appointmentId == null) return "redirect:/view-appointment";

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) return "redirect:/view-appointment";

        Appointment appointment = appointmentOpt.get();
        model.addAttribute("appointment", appointment);
        
        // Price logic (simplified for now, usually based on service)
        double price = 15.0; // Fixed price for demo
        model.addAttribute("price", price);

        return "customer/payment";
    }

    @PostMapping("/processPayment")
    public String processPayment(@RequestParam String paymentMethod,
                                 @RequestParam(required = false) String bankName,
                                 @RequestParam(required = false) String bankHolderName,
                                 HttpSession session) {
        
        Long appointmentId = (Long) session.getAttribute("lastAppointmentId");
        if (appointmentId == null) return "redirect:/view-appointment";

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) return "redirect:/view-appointment";

        Appointment appointment = appointmentOpt.get();
        double price = 15.0;

        Payment payment;
        if ("online".equals(paymentMethod)) {
            OnlinePayment op = new OnlinePayment();
            op.setAmount(price);
            op.setPaymentDate(new Date());
            op.setAppointmentId(appointmentId);
            op.setPaymentMethod("Online");
            op.setBankName(bankName);
            op.setBankHolderName(bankHolderName);
            payment = paymentRepository.save(op);
            
            appointment.setPaymentStatus("Completed");
        } else {
            CashPayment cp = new CashPayment();
            cp.setAmount(price);
            cp.setPaymentDate(new Date());
            cp.setAppointmentId(appointmentId);
            cp.setPaymentMethod("Cash");
            cp.setCashReceive(0.0); // Will be updated by staff
            payment = paymentRepository.save(cp);
            
            appointment.setPaymentStatus("Pending");
        }

        appointmentRepository.save(appointment);
        
        return "redirect:/receipt?appointmentId=" + appointmentId;
    }
}
