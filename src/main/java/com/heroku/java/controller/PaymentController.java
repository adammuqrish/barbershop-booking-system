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
    public String paymentPage(@RequestParam(required = false) Long appointmentId,
            HttpSession session,
            Model model) {

        // ✅ 1. AMBIL APPOINTMENT DARI SESSION
        Appointment appointment = (Appointment) session.getAttribute("pendingAppointment");

        // 2. Jika tak ada dalam session, redirect ke Booking
        if (appointment == null) {
            return "redirect:/booking";
        }

        model.addAttribute("appointment", appointment);

        // If already paid (unlikely if session based, but good safety check)
        // Note: appointment ID might be null here as it's not saved yet
        // So we skip the ID check for now or check status only if ID exists

        // ✅ 3. LOGIK KIRA HARGA (AMBIL DARI OBJEK SESSION)
        double price = 0.0;
        String category = appointment.getCustType();

        // Handle case kalau category null
        if (category == null)
            category = "Adult";

        switch (category) {
            case "Child":
                price = 10.0;
                break;
            case "Teen":
                price = 13.0;
                break;
            case "Senior":
                price = 12.0;
                break;
            case "Adult":
            default:
                price = 15.0;
                break;
        }

        // 4. Check Loyalty Points
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer != null) {
            // Reload from DB to get latest points
            customer = customerRepository.findById(customer.getCustId()).orElse(customer);
            if (customer.getCustLoyaltyPoints() != null && customer.getCustLoyaltyPoints() >= MAX_LOYALTY_POINTS) {
                price = 0.0;
                model.addAttribute("freeMessage",
                        "Congratulations! This appointment is FREE as you have reached loyalty reward.");
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

        // ✅ 1. AMBIL APPOINTMENT DARI SESSION
        Appointment appointment = (Appointment) session.getAttribute("pendingAppointment");

        // 2. Jika session hilang/tak ada, redirect ke booking
        if (appointment == null) {
            return "redirect:/booking";
        }

        // ✅ 3. PASTIKAN ID KOSONG (Supaya JPA create new record)
        appointment.setAppointmentId(null);

        // ✅ 4. KIRA HARGA (Sama macam atas)
        double price = 15.0;
        String category = appointment.getCustType();
        if (category == null)
            category = "Adult";

        switch (category) {
            case "Child":
                price = 10.0;
                break;
            case "Teen":
                price = 13.0;
                break;
            case "Senior":
                price = 12.0;
                break;
            case "Adult":
            default:
                price = 15.0;
                break;
        }

        // Check loyalty
        Long custId = appointment.getCustId();
        Optional<Customer> custOpt = customerRepository.findById(custId);
        if (custOpt.isPresent()) {
            Customer c = custOpt.get();
            if (c.getCustLoyaltyPoints() != null && c.getCustLoyaltyPoints() >= MAX_LOYALTY_POINTS) {
                price = 0.0;
            }
        }

        // ✅ 5. SIMPAN APPOINTMENT KE DATABASE (BARU SEKARANG)
        Appointment savedAppointment = appointmentRepository.save(appointment);
        Long newAppointmentId = savedAppointment.getAppointmentId();

        // ✅ 6. HANDLE PAYMENT
        Payment payment;
        if ("online".equals(paymentMethod)) {
            OnlinePayment op = new OnlinePayment();
            op.setAmount(java.math.BigDecimal.valueOf(price));
            op.setPaymentDate(LocalDate.now());
            op.setAppointmentId(newAppointmentId); // Guna ID baru
            op.setPaymentMethod("online-banking");
            op.setBankName(bankName);
            op.setBankHolderName(bankHolderName);
            payment = paymentRepository.save(op);

            savedAppointment.setPaymentStatus("completed");

            // Update Loyalty Points
            customerRepository.findById(custId).ifPresent(customer -> {
                int currentPoints = customer.getCustLoyaltyPoints() == null ? 0 : customer.getCustLoyaltyPoints();
                int newPoints = (currentPoints % MAX_LOYALTY_POINTS) + 1;
                customer.setCustLoyaltyPoints(newPoints);
                customerRepository.save(customer);

                // Update session user
                Customer sessionCustomer = (Customer) session.getAttribute("customer");
                if (sessionCustomer != null && sessionCustomer.getCustId().equals(custId)) {
                    session.setAttribute("customer", customer);
                }
            });

        } else {
            CashPayment cp = new CashPayment();
            cp.setAmount(java.math.BigDecimal.valueOf(price));
            cp.setPaymentDate(LocalDate.now());
            cp.setAppointmentId(newAppointmentId); // Guna ID baru
            cp.setPaymentMethod("cash");
            cp.setCashReceive(0.0);
            payment = paymentRepository.save(cp);

            savedAppointment.setPaymentStatus("pending");
        }

        appointmentRepository.save(savedAppointment);

        // ✅ 7. CLEAR SESSION (PENTING!)
        session.removeAttribute("pendingAppointment");

        return "redirect:/receipt?appointmentId=" + newAppointmentId + "&source=booking";
    }
}
