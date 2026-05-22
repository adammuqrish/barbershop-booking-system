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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
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

    @GetMapping("/login")
    public String loginPage(HttpSession session, 
                           @RequestParam(required = false) String error,
                           Model model) {
        if (session.getAttribute("customer") != null || session.getAttribute("loggedInStaff") != null) {
            return "redirect:/index";
        }
        if (error != null) {
            model.addAttribute("error", "Invalid credentials");
        }
        return "customer/register";
    }

    // --- HANDLE CUSTOMER LOGIN & REGISTER ---
    @PostMapping("/auth")
    public String handleAuth(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String email, // Login form sends "email"
            @RequestParam(required = false) String password, // Login form sends "password"
            @RequestParam(required = false) String name, // Registration form sends "name"
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String confirmPassword,
            HttpSession session,
            Model model) {

        // --- SECTION 1: LOGIN CUSTOMER ---
        if ("login".equals(action)) {
            logger.debug("Customer login request - Action: {}, Email: {}, Password: {}", action, email, password != null ? "[PROVIDED]" : "[NULL]");
            
            if (email == null || email.trim().isEmpty()) {
                logger.error("Email is empty or null!");
                model.addAttribute("error", "Email cannot be empty");
                return "customer/register";
            }
            
            if (password == null || password.trim().isEmpty()) {
                logger.error("Password is empty or null!");
                model.addAttribute("error", "Password cannot be empty");
                return "customer/register";
            }

            Optional<Customer> customerOpt = customerService.login(email, password);
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                logger.debug("Customer login successful - ID: {}, Email: {}", customer.getCustId(),
                        customer.getCustEmail());

                // 1. Set Session
                session.setAttribute("customer", customer);
                session.setAttribute("custId", customer.getCustId());
                logger.debug("Session attributes set for customer ID: {}", customer.getCustId());

                // 2. SET SPRING SECURITY CONTEXT with ROLE_CUSTOMER authority
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        customer.getCustEmail(),
                        null,
                        authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                // 3. CRITICAL: Save context to session
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                logger.debug("Spring Security context set for customer: {}", customer.getCustEmail());

                return "redirect:/index";
            }
        }

        // --- SECTION 2: REGISTER CUSTOMER ---
        else if ("register".equals(action)) {

            // --- BAHAGIAN VALIDASI ---

            if (name == null || name.trim().isEmpty()) {
                model.addAttribute("error", "Please enter your name.");
                return "customer/register";
            }

            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Please enter your email.");
                return "customer/register";
            }

            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                model.addAttribute("error", "Please enter a valid email format.");
                return "customer/register";
            }

            if (phone == null || phone.trim().isEmpty()) {
                model.addAttribute("error", "Please enter your phone number.");
                return "customer/register";
            }

            if (customerService.findByCustPhoneNumber(phone) != null) {
                model.addAttribute("error", "Phone number already exist");
                return "customer/register";
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "Please enter your password.");
                return "customer/register";
            }

            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                model.addAttribute("error", "Please confirm your password.");
                return "customer/register";
            }

            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Password do not match");
                return "customer/register";
            }

            // --- SIMPAN DATA ---
            Customer customer = new Customer();
            customer.setCustName(name);
            customer.setCustEmail(email);
            customer.setCustPassword(password);
            customer.setCustPhoneNumber(phone);
            customer.setCustLoyaltyPoints(0);

            try {
                customerService.registerCustomer(customer);
                model.addAttribute("successMessage", "Registration successful! You can now login.");
                return "customer/register";
            } catch (Exception e) {
                model.addAttribute("error", "Registration failed. Email might already exist.");
                return "customer/register";
            }
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
        SecurityContextHolder.clearContext(); // Pastikan ini ada
        return "redirect:/index";
    }
}
