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
            RedirectAttributes redirectAttributes) { // ✅ TAMBAH PARAMETER NI

        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        Optional<Customer> customerOpt = customerRepository.findById(custId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // ✅ LOGIK CHECK PASSWORD SAMA
            if (password != null && !password.isEmpty()) {

                // Kita perlu compare password yang dimasukkan user dengan password yang ada
                // dalam DB (yang dah di-hash)
                // Guna PasswordEncoder
                org.springframework.security.crypto.password.PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

                // check: Password Baru == Password Lama (dalam DB)?
                if (encoder.matches(password, customer.getCustPassword())) {
                    // Password SAMA -> TAK BOLEH UPDATE
                    redirectAttributes.addFlashAttribute("error",
                            "New password cannot be the same as the current password.");
                    return "redirect:/edit-profile";
                }

                // Password BERBEZA -> BOLEH UPDATE
                customer.setCustPassword(password);
                customerService.registerCustomer(customer); // Will hash the password
            }

            // Update detail lain
            customer.setCustName(name);
            customer.setCustEmail(email);
            customer.setCustPhoneNumber(phone);

            // Handle Image Upload
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
                }
            }

            // Simpan ke DB (Jika password tak diupdate)
            if (password == null || password.isEmpty()) {
                customerRepository.save(customer);
            }

            session.setAttribute("customer", customer);

            // Message success jika tiada error
            if (!redirectAttributes.containsAttribute("error")) {
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
            }
        }

        return "redirect:/profile";
    }
}
