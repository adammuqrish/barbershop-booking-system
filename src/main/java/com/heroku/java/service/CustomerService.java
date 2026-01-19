package com.heroku.java.service;

import com.heroku.java.model.Customer;
import com.heroku.java.repository.CustomerRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

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
        Optional<Customer> customerOpt = customerRepository.findByCustEmail(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            String storedPassword = customer.getCustPassword();

            try {
                // BCrypt hashes normally start with $2a$, $2y$, or $2b$
                if (storedPassword != null && (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2y$")
                        || storedPassword.startsWith("$2b$"))) {
                    if (BCrypt.checkpw(password, storedPassword)) {
                        return Optional.of(customer);
                    }
                } else {
                    // Legacy check: for plain text passwords from old system
                    if (password.equals(storedPassword)) {
                        // Upgrade to BCrypt automatically
                        customer.setCustPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                        customerRepository.save(customer);
                        return Optional.of(customer);
                    }
                }
            } catch (Exception e) {
                // If anything goes wrong with BCrypt (like invalid salt version), fallback to
                // plain text check
                if (password.equals(storedPassword)) {
                    customer.setCustPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                    customerRepository.save(customer);
                    return Optional.of(customer);
                }
            }
        }
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
