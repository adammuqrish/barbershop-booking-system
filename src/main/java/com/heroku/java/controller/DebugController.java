package com.heroku.java.controller;

import com.heroku.java.model.Customer;
import com.heroku.java.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    private final CustomerRepository customerRepository;

    @Autowired
    public DebugController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/debug/customers")
    public Map<String, Object> debugCustomers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.debug("Debug: Fetching all customers");
            List<Customer> customers = (List<Customer>) customerRepository.findAll();
            response.put("totalCustomers", customers.size());
            response.put("customers", customers.stream()
                .map(c -> Map.of(
                    "id", c.getCustId(),
                    "email", c.getCustEmail(),
                    "name", c.getCustName(),
                    "passwordStartsWith", c.getCustPassword() != null && c.getCustPassword().length() >= 4 ? 
                        c.getCustPassword().substring(0, 4) : "NULL"
                ))
                .toList());
            response.put("status", "success");
            logger.debug("Debug: Found {} customers", customers.size());
        } catch (Exception e) {
            logger.error("Debug: Error fetching customers", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/debug/customer")
    public Map<String, Object> debugCustomerByEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.debug("Debug: Searching for customer with email: {}", email);
            Optional<Customer> customerOpt = customerRepository.findByCustEmail(email);
            
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                response.put("found", true);
                response.put("customer", Map.of(
                    "id", customer.getCustId(),
                    "email", customer.getCustEmail(),
                    "name", customer.getCustName(),
                    "phone", customer.getCustPhoneNumber(),
                    "passwordLength", customer.getCustPassword() != null ? customer.getCustPassword().length() : 0,
                    "passwordStartsWith", customer.getCustPassword() != null && customer.getCustPassword().length() >= 4 ? 
                        customer.getCustPassword().substring(0, 4) : "NULL",
                    "loyaltyPoints", customer.getCustLoyaltyPoints()
                ));
                logger.debug("Debug: Customer found - ID: {}", customer.getCustId());
            } else {
                response.put("found", false);
                response.put("message", "No customer found with email: " + email);
                logger.debug("Debug: No customer found with email: {}", email);
            }
            response.put("status", "success");
        } catch (Exception e) {
            logger.error("Debug: Error searching for customer", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/debug/test-password")
    public Map<String, Object> testPassword(@RequestParam String email, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.debug("Debug: Testing password for email: {}", email);
            Optional<Customer> customerOpt = customerRepository.findByCustEmail(email);
            
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                String storedPassword = customer.getCustPassword();
                
                response.put("customerFound", true);
                response.put("customerId", customer.getCustId());
                response.put("storedPasswordLength", storedPassword.length());
                response.put("storedPasswordStartsWith", storedPassword.substring(0, 4));
                
                // Test BCrypt
                boolean isBcrypt = storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2y$") || storedPassword.startsWith("$2b$");
                response.put("isBcrypt", isBcrypt);
                
                if (isBcrypt) {
                    try {
                        boolean bcryptResult = org.mindrot.jbcrypt.BCrypt.checkpw(password, storedPassword);
                        response.put("bcryptCheck", bcryptResult);
                        logger.debug("Debug: BCrypt check result: {}", bcryptResult);
                    } catch (Exception e) {
                        response.put("bcryptError", e.getMessage());
                        logger.error("Debug: BCrypt error", e);
                    }
                } else {
                    // Test plain text
                    boolean plainTextResult = password.equals(storedPassword);
                    response.put("plainTextCheck", plainTextResult);
                    logger.debug("Debug: Plain text check result: {}", plainTextResult);
                }
            } else {
                response.put("customerFound", false);
                response.put("message", "No customer found with email: " + email);
            }
            response.put("status", "success");
        } catch (Exception e) {
            logger.error("Debug: Error testing password", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}