package com.heroku.java.controller;

import com.heroku.java.model.Customer;
import com.heroku.java.model.Staff;
import com.heroku.java.service.CustomerService;
import com.heroku.java.service.StaffService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    private final CustomerService customerService;
    private final StaffService staffService;

    @Autowired
    public AuthController(CustomerService customerService, StaffService staffService) {
        this.customerService = customerService;
        this.staffService = staffService;
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("customer") != null) {
            return "redirect:/index";
        }
        return "customer/register";
    }

    @GetMapping("/adminLogin")
    public String adminLoginPage(HttpSession session) {
        if (session.getAttribute("loggedInStaff") != null) {
            return "redirect:/adminIndex";
        }
        return "admin/adminLogin";
    }

    @PostMapping("/auth")
    public String handleAuth(@RequestParam String action,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String confirmPassword,
            HttpSession session,
            Model model) {

        if ("login".equals(action)) {
            Optional<Customer> customerOpt = customerService.login(email, password);
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                session.setAttribute("customer", customer);
                session.setAttribute("custId", customer.getCustId());
                return "redirect:/index";
            } else {
                model.addAttribute("error", "Invalid email or password");
                return "customer/register";
            }
        } else if ("register".equals(action)) {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match.");
                return "customer/register";
            }
            Customer customer = new Customer();
            customer.setCustName(name);
            customer.setCustEmail(email);
            customer.setCustPassword(password);
            customer.setCustPhoneNumber(phone);
            customer.setCustLoyaltyPoints(0);

            try {
                customerService.registerCustomer(customer);
                model.addAttribute("successMessage", "Registration successful! You can now login.");
            } catch (Exception e) {
                model.addAttribute("error", "Registration failed. Email might already exist.");
            }
            return "customer/register";
        }
        return "redirect:/register";
    }

    @PostMapping("/staffAuth")
    public String handleStaffAuth(
            @RequestParam("email") String staffEmail,
            @RequestParam("password") String staffPassword,
            HttpSession session,
            Model model) {

        Optional<Staff> staffOpt = staffService.login(staffEmail, staffPassword);

        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();

            // 1. Set Authorities (Role)
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + staff.getStaffRole()));

            // 2. Create Authentication Token
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    staffEmail, // Principal (Username/Email)
                    null, // Credentials (Password tak perlu disimpan sini)
                    authorities // Authories
            );

            // 3. Set Security Context
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 4. CRITICAL: Save Context to Session
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Optional: Simpan staffId dalam session untuk kegunaan kod lama (jika ada)
            session.setAttribute("staffId", staff.getStaffId());

            System.out.println("STAFF AUTH HIT - Role: " + staff.getStaffRole());

            // 5. ✅ LOGIK REDIRECT BARU
            if ("ADMIN".equalsIgnoreCase(staff.getStaffRole())) {
                return "redirect:/adminIndex";
            } else if ("BARBER".equalsIgnoreCase(staff.getStaffRole())) {
                return "redirect:/barber/dashboard";
            } else {
                // Default fallback (kalau ada role lain)
                return "redirect:/";
            }
        }

        model.addAttribute("loginError", "Invalid email or password");
        return "admin/adminLogin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/index";
    }
}
