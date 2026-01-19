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

    @Autowired
    public ProfileController(CustomerRepository customerRepository, CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
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

            // Handle Image Upload (Kod Asal)
            if (image != null && !image.isEmpty()) {
                try {
                    String fileName = java.util.UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                    java.nio.file.Path uploadPath = java.nio.file.Paths
                            .get("src/main/resources/static/resources/uploads");

                    if (!java.nio.file.Files.exists(uploadPath)) {
                        java.nio.file.Files.createDirectories(uploadPath);
                    }

                    java.nio.file.Path filePath = uploadPath.resolve(fileName);
                    java.nio.file.Files.copy(image.getInputStream(), filePath,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    customer.setCustPicture(fileName);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Failed to upload image.");
                    return "redirect:/edit-profile"; // <<<< Tambah redirect ni jika upload gagal
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
