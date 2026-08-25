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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ProfileController {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final com.heroku.java.service.FileStorageService fileStorageService;

    @Autowired
    public ProfileController(CustomerRepository customerRepository, CustomerService customerService,
            com.heroku.java.service.FileStorageService fileStorageService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        Optional<Customer> customerOpt = customerRepository.findById(custId);
        if (customerOpt.isEmpty())
            return "redirect:/index";

        model.addAttribute("customer", customerOpt.get());
        return "customer/profile";
    }

    @GetMapping("/edit-profile")
    public String editProfilePage(HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        Optional<Customer> customerOpt = customerRepository.findById(custId);
        if (customerOpt.isEmpty())
            return "redirect:/index";

        model.addAttribute("customer", customerOpt.get());
        return "customer/editProfile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam(required = false) String password,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        Optional<Customer> customerOpt = customerRepository.findById(custId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // --- VALIDASI DUPLIKAT PHONE (BUG FIX) ---

            String oldPhone = customer.getCustPhoneNumber();

            // Semak hanya jika nombor telefon ditukar
            if (!phone.equals(oldPhone)) {
                Customer existingCustomer = customerService.findByCustPhoneNumber(phone); // Gunakan method yang kita
                                                                                          // tambah tadi

                // Jika nombor wujud DAN ia bukan milik diri sendiri
                if (existingCustomer != null && !existingCustomer.getCustId().equals(custId)) {
                    redirectAttributes.addFlashAttribute("error", "Phone number already exist");
                    return "redirect:/edit-profile";
                }
            }
            // ------------------------------------------

            // --- LOGIK CHECK PASSWORD (KOD ASAL) ---
            if (password != null && !password.isEmpty()) {
                org.springframework.security.crypto.password.PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

                if (encoder.matches(password, customer.getCustPassword())) {
                    redirectAttributes.addFlashAttribute("error",
                            "New password cannot be the same as the current password.");
                    return "redirect:/edit-profile";
                }

                customer.setCustPassword(password);
                customerService.registerCustomer(customer);
            }

            // Update detail lain
            customer.setCustName(name);
            customer.setCustEmail(email);
            customer.setCustPhoneNumber(phone);

            // Handle Image Upload (validated, stored in external upload dir)
            if (image != null && !image.isEmpty()) {
                try {
                    String fileName = fileStorageService.storeImage(image);
                    customer.setCustPicture(fileName);
                } catch (IllegalArgumentException e) {
                    redirectAttributes.addFlashAttribute("error", e.getMessage());
                    return "redirect:/edit-profile";
                } catch (java.io.IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Failed to upload image.");
                    return "redirect:/edit-profile";
                }
            }

            // Simpan ke DB
            if (password == null || password.isEmpty()) {
                customerRepository.save(customer);
            }

            session.setAttribute("customer", customer);

            // Message success jika tiada error
            if (!redirectAttributes.containsAttribute("error")) {
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
                return "redirect:/profile";
            }
        }

        return "redirect:/profile";
    }
}
