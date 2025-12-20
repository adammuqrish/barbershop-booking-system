package com.heroku.java.controller;

import com.heroku.java.model.Customer;
import com.heroku.java.repository.CustomerRepository;
import com.heroku.java.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ProfileController {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @Autowired
    public ProfileController(CustomerRepository customerRepository, CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null) return "redirect:/register";

        Optional<Customer> customerOpt = customerRepository.findById(custId);
        if (customerOpt.isEmpty()) return "redirect:/index";

        model.addAttribute("customer", customerOpt.get());
        return "customer/profile";
    }

    @GetMapping("/edit-profile")
    public String editProfilePage(HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null) return "redirect:/register";

        Optional<Customer> customerOpt = customerRepository.findById(custId);
        if (customerOpt.isEmpty()) return "redirect:/index";

        model.addAttribute("customer", customerOpt.get());
        return "customer/editProfile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phone,
                                @RequestParam(required = false) String password,
                                HttpSession session) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null) return "redirect:/register";

        Optional<Customer> customerOpt = customerRepository.findById(custId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setCustName(name);
            customer.setCustPhoneNumber(phone);
            if (password != null && !password.isEmpty()) {
                customer.setCustPassword(password);
                customerService.registerCustomer(customer); // This hashes the password
            } else {
                customerRepository.save(customer);
            }
            session.setAttribute("customer", customer);
        }

        return "redirect:/profile";
    }
}
