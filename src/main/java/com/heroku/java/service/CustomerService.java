package com.heroku.java.service;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.Customer;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.CustomerRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    /** Points needed to unlock one free (reward) cut. */
    public static final int MAX_LOYALTY_POINTS = 2;

    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
            AppointmentRepository appointmentRepository) {
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Customer registerCustomer(Customer customer) {
        customer.setCustPassword(BCrypt.hashpw(customer.getCustPassword(), BCrypt.gensalt()));
        return customerRepository.save(customer);
    }

public Optional<Customer> login(String email, String password) {
        logger.debug("Customer login attempt - Email: {}", email);
        
        Optional<Customer> customerOpt = customerRepository.findByCustEmail(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            String storedPassword = customer.getCustPassword();

            try {
                // BCrypt hashes normally start with $2a$, $2y$, or $2b$
                if (storedPassword != null && (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2y$")
                        || storedPassword.startsWith("$2b$"))) {
                    if (BCrypt.checkpw(password, storedPassword)) {
                        logger.debug("BCrypt verification successful");
                        return Optional.of(customer);
                    }
                } else if (storedPassword != null && password.equals(storedPassword)) {
                    // Legacy migration: upgrade old plain-text passwords to BCrypt
                    customer.setCustPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                    customerRepository.save(customer);
                    logger.debug("Legacy plain-text password upgraded to BCrypt");
                    return Optional.of(customer);
                }
            } catch (Exception e) {
                logger.error("Exception during password verification: {}", e.getMessage());
            }
        }
        logger.debug("Login failed for email: {}", email);
        return Optional.empty();
    }

    public Customer updateProfile(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer findByCustPhoneNumber(String phone) {
        // Anda perlu panggil method yang sama dalam Repository
        return customerRepository.findByCustPhoneNumber(phone);
    }

    // ============================================================
    // Loyalty points lifecycle (single source of truth)
    //
    // Reward is CONSUMED AT CHECKOUT of the free appointment, not when the
    // service completes. This closes the loophole where a customer could book
    // several appointments right after earning a reward and get all of them
    // free, because the balance only dropped once an appointment completed.
    // ============================================================

    /**
     * Checkout of a free reward cut: consume the earned balance immediately so
     * any further booking made before this appointment completes is charged
     * normally.
     */
    public void redeemReward(Long custId) {
        customerRepository.findById(custId).ifPresent(customer -> {
            customer.setCustLoyaltyPoints(0);
            customerRepository.save(customer);
            logger.debug("Loyalty reward redeemed at checkout for customer {}", custId);
        });
    }

    /**
     * Grants one point for an appointment that is both COMPLETED ('done') and
     * PAID ('completed'). Safe to call repeatedly or from any lifecycle
     * endpoint: value_loyalty is marked LOYALTY_AWARDED once granted, so status
     * flips or later payment confirmations can never double-award.
     */
    public void awardPointsForCompletedAppointment(Appointment a) {
        if (!"done".equalsIgnoreCase(a.getServiceStatus())) return;
        if (!"completed".equalsIgnoreCase(a.getPaymentStatus())) return;
        int flag = a.getValueLoyalty() == null ? Appointment.LOYALTY_NORMAL : a.getValueLoyalty();
        if (flag == Appointment.LOYALTY_AWARDED) return;

        customerRepository.findById(a.getCustId()).ifPresent(customer -> {
            int current = customer.getCustLoyaltyPoints() == null
                    ? 0 : customer.getCustLoyaltyPoints();
            // Every completed visit counts, including a redeemed free one
            // (balance was zeroed at its checkout, so it lands on 1).
            int newPoints = Math.min(current + 1, MAX_LOYALTY_POINTS);
            customer.setCustLoyaltyPoints(newPoints);
            customerRepository.save(customer);

            a.setValueLoyalty(Appointment.LOYALTY_AWARDED);
            appointmentRepository.save(a);
        });
    }

    /**
     * Restores a consumed reward when its appointment is cancelled before the
     * service completes, so the customer does not lose it. No-op unless the
     * appointment actually was a redeemed (free) cut; marking the appointment
     * back to NORMAL makes the refund idempotent.
     */
    public void refundRewardIfRedeemed(Appointment a) {
        int flag = a.getValueLoyalty() == null ? Appointment.LOYALTY_NORMAL : a.getValueLoyalty();
        if (flag != Appointment.LOYALTY_REDEEMED) return;

        customerRepository.findById(a.getCustId()).ifPresent(customer -> {
            int current = customer.getCustLoyaltyPoints() == null
                    ? 0 : customer.getCustLoyaltyPoints();
            if (current < MAX_LOYALTY_POINTS) {
                customer.setCustLoyaltyPoints(MAX_LOYALTY_POINTS);
                customerRepository.save(customer);
                logger.debug("Loyalty reward refunded after cancellation for customer {}", a.getCustId());
            }
            a.setValueLoyalty(Appointment.LOYALTY_NORMAL);
            appointmentRepository.save(a);
        });
    }
}
