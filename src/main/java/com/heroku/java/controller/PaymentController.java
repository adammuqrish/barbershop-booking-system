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

import java.time.LocalDate;
import java.util.Optional;

import com.heroku.java.model.Customer;
import com.heroku.java.repository.CustomerRepository;

@Controller
public class PaymentController {

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public PaymentController(AppointmentRepository appointmentRepository,
            PaymentRepository paymentRepository,
            CustomerRepository customerRepository) {
        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
    }

    private static final int MAX_LOYALTY_POINTS = 2;

    @GetMapping("/payment")
    public String paymentPage(HttpSession session, Model model) {
        Long appointmentId = (Long) session.getAttribute("lastAppointmentId");
        if (appointmentId == null)
            return "redirect:/view-appointment";

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty())
            return "redirect:/view-appointment";

        Appointment appointment = appointmentOpt.get();
        model.addAttribute("appointment", appointment);

        // Check Loyalty Points
        double price = 15.0; // Default price
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer != null) {
            // Reload customer from DB to get latest points
            customer = customerRepository.findById(customer.getCustId()).orElse(customer);
            if (customer.getCustLoyaltyPoints() != null && customer.getCustLoyaltyPoints() >= MAX_LOYALTY_POINTS) {
                price = 0.0;
                model.addAttribute("freeMessage",
                        "Congratulations! This appointment is FREE as you have reached the loyalty reward.");
            }
        }

        model.addAttribute("price", price);

        return "customer/payment";
    }

    @PostMapping("/processPayment")
    public String processPayment(@RequestParam String paymentMethod,
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String bankHolderName,
            HttpSession session) {

        Long appointmentId = (Long) session.getAttribute("lastAppointmentId");
        if (appointmentId == null)
            return "redirect:/view-appointment";

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty())
            return "redirect:/view-appointment";

        Appointment appointment = appointmentOpt.get();
        double price = 15.0;

        // Check loyalty for price calculation
        Optional<Customer> custOpt = customerRepository.findById(appointment.getCustId());
        if (custOpt.isPresent()) {
            Customer c = custOpt.get();
            if (c.getCustLoyaltyPoints() != null && c.getCustLoyaltyPoints() >= MAX_LOYALTY_POINTS) {
                price = 0.0;
            }
        }

        Payment payment;
        if ("online".equals(paymentMethod)) {
            OnlinePayment op = new OnlinePayment();
            op.setAmount(java.math.BigDecimal.valueOf(price));
            op.setPaymentDate(LocalDate.now());
            op.setAppointmentId(appointmentId);
            op.setPaymentMethod("online-banking");
            op.setBankName(bankName);
            op.setBankHolderName(bankHolderName);
            payment = paymentRepository.save(op);

            appointment.setPaymentStatus("completed");

            // Update Loyalty Points
            Long custId = appointment.getCustId();
            customerRepository.findById(custId).ifPresent(customer -> {
                int currentPoints = customer.getCustLoyaltyPoints() == null ? 0 : customer.getCustLoyaltyPoints();
                // Logic: 1->2 ... Max->(reset) 1
                int newPoints = (currentPoints % MAX_LOYALTY_POINTS) + 1;
                customer.setCustLoyaltyPoints(newPoints);
                customerRepository.save(customer);

                // Update session if the logged in user is this customer
                Customer sessionCustomer = (Customer) session.getAttribute("customer");
                if (sessionCustomer != null && sessionCustomer.getCustId().equals(custId)) {
                    session.setAttribute("customer", customer);
                }
            });

        } else {
            CashPayment cp = new CashPayment();
            cp.setAmount(java.math.BigDecimal.valueOf(price));
            cp.setPaymentDate(LocalDate.now());
            cp.setAppointmentId(appointmentId);
            cp.setPaymentMethod("cash");
            cp.setCashReceive(0.0); // Will be updated by staff
            payment = paymentRepository.save(cp);

            appointment.setPaymentStatus("pending");
        }

        appointmentRepository.save(appointment);

        return "redirect:/receipt?appointmentId=" + appointmentId + "&source=view-appointment";
    }
}
