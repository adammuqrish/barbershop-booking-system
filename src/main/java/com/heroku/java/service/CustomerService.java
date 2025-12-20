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
            if (BCrypt.checkpw(password, customer.getCustPassword())) {
                return Optional.of(customer);
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
            if (newPoints > 10) newPoints = 0;
            customer.setCustLoyaltyPoints(newPoints);
            customerRepository.save(customer);
        });
    }
}
