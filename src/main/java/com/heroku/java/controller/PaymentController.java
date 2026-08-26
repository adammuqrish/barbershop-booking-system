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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

import com.heroku.java.model.Customer;
import com.heroku.java.repository.CustomerRepository;
import com.heroku.java.service.CustomerService;

@Controller
public class PaymentController {

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @Autowired
    public PaymentController(AppointmentRepository appointmentRepository,
            PaymentRepository paymentRepository,
            CustomerRepository customerRepository,
            CustomerService customerService) {
        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    @GetMapping("/payment")
    public String paymentPage(@RequestParam(required = false) Long appointmentId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // ✅ 1. Consume any flash error from a failed submission
        String flashError = (String) redirectAttributes.getFlashAttributes().get("bookingError");
        if (flashError == null) {
            flashError = (String) session.getAttribute("flashError");
            if (flashError != null) {
                session.removeAttribute("flashError");
            }
        }
        if (flashError != null) model.addAttribute("bookingError", flashError);

        // ✅ 2. AMBIL APPOINTMENT DARI SESSION
        Appointment appointment = (Appointment) session.getAttribute("pendingAppointment");

        // 3. Jika tak ada dalam session, redirect ke booking
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
            if (customer.getCustLoyaltyPoints() != null
                    && customer.getCustLoyaltyPoints() >= CustomerService.MAX_LOYALTY_POINTS) {
                price = 0.0;
                model.addAttribute("freeMessage",
                        "Congratulations! This appointment is FREE as you have reached loyalty reward.");
            }
        }

        model.addAttribute("price", price);

        return "customer/payment";
    }

    @PostMapping("/processPayment")
    public String processPayment(@RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String bankHolderName,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ✅ 1. AMBIL APPOINTMENT DARI SESSION
        Appointment appointment = (Appointment) session.getAttribute("pendingAppointment");

        // 2. Jika tak ada dalam session, redirect ke booking
        if (appointment == null) {
            return "redirect:/booking";
        }

        // ✅ 3. VALIDATE paymentMethod
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("bookingError", "Please select a payment method before continuing.");
            return "redirect:/payment";
        }
        if (!"cash".equals(paymentMethod) && !"online".equals(paymentMethod)) {
            redirectAttributes.addFlashAttribute("bookingError", "Invalid payment method. Please choose cash or online.");
            return "redirect:/payment";
        }

        // ✅ 4. Validate bank details if online payment
        if ("online".equals(paymentMethod)) {
            if (bankName == null || bankName.trim().isEmpty()
                    || bankHolderName == null || bankHolderName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("bookingError", "Please fill in all bank details for online payment.");
                return "redirect:/payment";
            }
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
            if (c.getCustLoyaltyPoints() != null
                    && c.getCustLoyaltyPoints() >= CustomerService.MAX_LOYALTY_POINTS) {
                price = 0.0;
                // Mark THIS appointment as the redeemed reward cut and consume the
                // balance NOW. Consuming at checkout (not at completion) means any
                // further booking made before this appointment completes is charged
                // normally - the reward cannot be reused until re-earned.
                appointment.setValueLoyalty(Appointment.LOYALTY_REDEEMED);
                customerService.redeemReward(custId);
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

            // NOTE: Loyalty points are NOT awarded here anymore.
            // Points are granted only when the service is marked 'done'
            // (see AdminController.updateServiceStatus) so customers earn
            // points for completed cuts, not merely for paying up front.

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

        return "redirect:/receipt?appointmentId=" + newAppointmentId + "&source=view-appointment";
    }
}
