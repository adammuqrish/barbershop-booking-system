package com.heroku.java.service;

import com.heroku.java.model.Customer;
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
    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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
            
            logger.debug("Customer found - ID: {}, Email: {}", customer.getCustId(), customer.getCustEmail());
            logger.debug("Stored password starts with: {}", 
                storedPassword != null && storedPassword.length() >= 4 ? storedPassword.substring(0, 4) : "NULL");

            try {
                // BCrypt hashes normally start with $2a$, $2y$, or $2b$
                if (storedPassword != null && (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2y$")
                        || storedPassword.startsWith("$2b$"))) {
                    logger.debug("Attempting BCrypt password verification");
                    if (BCrypt.checkpw(password, storedPassword)) {
                        logger.debug("BCrypt verification successful");
                        return Optional.of(customer);
                    } else {
                        logger.debug("BCrypt verification failed - password mismatch");
                    }
                } else {
                    logger.debug("Stored password is not BCrypt format, attempting plain text verification");
                    // Legacy check: for plain text passwords from old system
                    if (password.equals(storedPassword)) {
                        logger.debug("Plain text verification successful, upgrading to BCrypt");
                        // Upgrade to BCrypt automatically
                        customer.setCustPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                        customerRepository.save(customer);
                        return Optional.of(customer);
                    } else {
                        logger.debug("Plain text verification failed - password mismatch");
                    }
                }
            } catch (Exception e) {
                logger.error("Exception during password verification: {}", e.getMessage(), e);
                // If anything goes wrong with BCrypt (like invalid salt version), fallback to
                // plain text check
                if (password.equals(storedPassword)) {
                    logger.debug("Fallback plain text verification successful, upgrading to BCrypt");
                    customer.setCustPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                    customerRepository.save(customer);
                    return Optional.of(customer);
                } else {
                    logger.debug("Fallback plain text verification failed");
                }
            }
        } else {
            logger.debug("No customer found with email: {}", email);
        }
        logger.debug("Login failed for email: {}", email);
        return Optional.empty();
    }

    public Customer updateProfile(Customer customer) {
        return customerRepository.save(customer);
    }

    public void updateLoyaltyPoints(Long custId, int points) {
        customerRepository.findById(custId).ifPresent(customer -> {
            int newPoints = customer.getCustLoyaltyPoints() + points;
            if (newPoints > 10)
                newPoints = 0;
            customer.setCustLoyaltyPoints(newPoints);
            customerRepository.save(customer);
        });
    }

    public Customer findByCustPhoneNumber(String phone) {
        // Anda perlu panggil method yang sama dalam Repository
        return customerRepository.findByCustPhoneNumber(phone);
    }
}
