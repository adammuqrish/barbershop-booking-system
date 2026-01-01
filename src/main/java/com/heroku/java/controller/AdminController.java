package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.CashPayment;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Staff;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.CustomerRepository;
import com.heroku.java.repository.StaffRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.heroku.java.repository.PaymentRepository;
import com.heroku.java.model.Payment;
import com.heroku.java.dto.TransactionDTO;
import com.heroku.java.dto.FeedbackDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AdminController {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final com.heroku.java.repository.FeedbackRepository feedbackRepository;

    @Autowired
    public AdminController(CustomerRepository customerRepository,
            StaffRepository staffRepository,
            AppointmentRepository appointmentRepository,
            PaymentRepository paymentRepository,
            com.heroku.java.repository.FeedbackRepository feedbackRepository) {
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/adminIndex")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminIndex(Model model) {
        // Load staff from authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<com.heroku.java.model.Staff> staffOpt = staffRepository.findByStaffEmail(email);
            if (staffOpt.isPresent()) {
                com.heroku.java.model.Staff staff = staffOpt.get();
                model.addAttribute("staffName", staff.getStaffName());
                model.addAttribute("staffRole", staff.getStaffRole());
                model.addAttribute("staff", staff);
            } else {
                model.addAttribute("staffName", "Staff");
                model.addAttribute("staffRole", null);
            }
        } else {
            model.addAttribute("staffName", "Staff");
            model.addAttribute("staffRole", null);
        }

        // Get total sales as BigDecimal
        java.math.BigDecimal totalSales = paymentRepository.getTotalSales();
        if (totalSales == null) {
            totalSales = java.math.BigDecimal.ZERO;
        }
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("customerCount", customerRepository.count());
        model.addAttribute("totalAppointments", appointmentRepository.count());
        model.addAttribute("customerList", customerRepository.findAll());

        // Mock sales by day for now
        Map<String, Double> salesByDay = new HashMap<>();
        salesByDay.put("SUNDAY", 0.0);
        salesByDay.put("MONDAY", 0.0);
        salesByDay.put("TUESDAY", 0.0);
        salesByDay.put("WEDNESDAY", 0.0);
        salesByDay.put("THURSDAY", 0.0);
        salesByDay.put("FRIDAY", 0.0);
        salesByDay.put("SATURDAY", 0.0);
        model.addAttribute("salesByDay", salesByDay);

        return "admin/adminIndex";
    }
    
    @GetMapping("/listCustomer")
    @PreAuthorize("hasRole('ADMIN')")
    public String listCustomers(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long custId,
            Model model) {
        // Load staff role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<Staff> staffOpt = staffRepository.findByStaffEmail(email);
            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                model.addAttribute("staffName", staff.getStaffName());
                model.addAttribute("staffRole", staff.getStaffRole());
                model.addAttribute("staff", staff);
            } else {
                model.addAttribute("staffName", "Staff");
                model.addAttribute("staffRole", null);
            }
        } else {
            model.addAttribute("staffName", "Staff");
            model.addAttribute("staffRole", null);
        }

        model.addAttribute("customerList", customerRepository.findAll());

        if (custId != null) {
            Optional<Customer> customer = customerRepository.findById(custId);
            customer.ifPresent(c -> model.addAttribute("customer", c));
        }

        return "admin/listCustomer";
    }

    @GetMapping("/listBarber")
    @PreAuthorize("hasRole('ADMIN')")
    public String listBarber(
            @RequestParam(required = false) Long staffId,
            Model model) {

        // ✅ Get logged-in admin info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            staffRepository.findByStaffEmail(auth.getName()).ifPresent(admin -> {
                model.addAttribute("staffName", admin.getStaffName());
                model.addAttribute("staffRole", admin.getStaffRole()); // ✅ IMPORTANT
                model.addAttribute("loggedInStaff", admin);
            });
        }

        // ✅ Barber list
        model.addAttribute("barberList", staffRepository.findAll());

        // ✅ Selected barber
        if (staffId != null) {
            staffRepository.findById(staffId)
                .ifPresent(s -> model.addAttribute("barber", s));
        }

        // ✅ Map adminId → adminName
        Map<Long, String> adminNameMap = new HashMap<>();
        for (Staff s : staffRepository.findAll()) {
            if (s.getAdminId() != null) {
                staffRepository.findById(s.getAdminId())
                    .ifPresent(admin ->
                        adminNameMap.put(s.getStaffId(), admin.getStaffName()));
            }
        }
        model.addAttribute("adminNameMap", adminNameMap);

        return "admin/listBarber";
    }

    @GetMapping("/listAppointment")
    @PreAuthorize("hasRole('ADMIN')")
    public String listAppointments(
            @RequestParam(required = false) Long appointmentId,
            Model model,
            Authentication authentication) {

        // ✅ logged-in admin info (for header)
        staffRepository.findByStaffEmail(authentication.getName())
                .ifPresent(admin -> {
                    model.addAttribute("staffName", admin.getStaffName());
                    model.addAttribute("staffRole", admin.getStaffRole());
                    model.addAttribute("loggedInStaff", admin);
                });

        // ✅ appointment list
       Iterable<Appointment> appointments = appointmentRepository.findAll();

        for (Appointment a : appointments) {

            // customer name
            customerRepository.findById(a.getCustId())
                    .ifPresent(c -> a.setCustomerName(c.getCustName()));

            // ✅ GET PAYMENT BY APPOINTMENT
            paymentRepository.findByAppointmentId(a.getAppointmentId())
                .ifPresent(p -> {

                    String method = p.getPaymentMethod();

                    if ("online-banking".equalsIgnoreCase(method)) {
                        a.setPaymentMethod("ONLINE");
                        a.setPaymentStatus("completed");
                    } else if ("cash".equalsIgnoreCase(method)) {
                        a.setPaymentMethod("CASH");
                        a.setPaymentStatus(a.getPaymentStatus());
                    }
                });
        }

        model.addAttribute("appointmentList", appointments);

        // ✅ selected appointment (for edit panel)
        if (appointmentId != null) {
            appointmentRepository.findById(appointmentId)
                    .ifPresent(a -> {
                        customerRepository.findById(a.getCustId())
                                .ifPresent(c -> a.setCustomerName(c.getCustName()));

                        model.addAttribute("appointment", a);
                    });
        }

        // ✅ barber dropdown
        model.addAttribute("barberList",
                staffRepository.findByStaffRole("BARBER"));

        return "admin/listAppointment";
    }

    @PostMapping("/admin/update-service-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateServiceStatus(@RequestParam Long appointmentId,
                                    @RequestParam String status,
                                    Authentication authentication) {

        appointmentRepository.findById(appointmentId).ifPresent(a -> {

            a.setServiceStatus(status);

            Staff admin = staffRepository
                    .findByStaffEmail(authentication.getName())
                    .orElse(null);

            if (admin != null) {
                a.setUpdatedBy(admin.getStaffId());
                a.setUpdatedAt(java.time.LocalDateTime.now());
            }

            appointmentRepository.save(a);
        });

        return "redirect:/listAppointment";
    }

    @PostMapping("/admin/update-payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String updatePaymentStatus(@RequestParam Long appointmentId,
                                    @RequestParam String status) {

        paymentRepository.findByAppointmentId(appointmentId).ifPresent(p -> {

            // ✅ ONLY CASH PAYMENT CAN BE UPDATED
            if (p instanceof CashPayment) {

                // update appointment payment status
                appointmentRepository.findById(appointmentId)
                        .ifPresent(a -> {
                            a.setPaymentStatus(status);
                            appointmentRepository.save(a);
                        });
            }
        });

        return "redirect:/listAppointment";
    }

    @GetMapping("/admin/list-transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public String listTransactions(Model model) {
        // Load staff role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<Staff> staffOpt = staffRepository.findByStaffEmail(email);
            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                model.addAttribute("staffName", staff.getStaffName());
                model.addAttribute("staffRole", staff.getStaffRole());
                model.addAttribute("staff", staff);
            } else {
                model.addAttribute("staffName", "Staff");
                model.addAttribute("staffRole", null);
            }
        } else {
            model.addAttribute("staffName", "Staff");
            model.addAttribute("staffRole", null);
        }

        Iterable<Payment> payments = paymentRepository.findAll();

        List<TransactionDTO> transactions = new ArrayList<>();
        for (Payment p : payments) {
            appointmentRepository.findById(p.getAppointmentId()).ifPresent(appt -> {
                customerRepository.findById(appt.getCustId()).ifPresent(cust -> {
                    TransactionDTO dto = new TransactionDTO();
                    dto.setPaymentId(p.getPaymentId());
                    dto.setCustomerName(cust.getCustName());
                    dto.setAmount(p.getAmount());
                    dto.setPaymentMethod(p.getPaymentMethod());
                    dto.setPaymentDate(p.getPaymentDate());
                    dto.setPaymentStatus(appt.getPaymentStatus());
                    transactions.add(dto);
                });
            });
        }

        model.addAttribute("transactions", transactions);
        return "admin/listTransactions";
    }

    @GetMapping("/admin/list-feedback")
    @PreAuthorize("hasRole('ADMIN')")
    public String listFeedback(Model model) {
        // Load staff role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<Staff> staffOpt = staffRepository.findByStaffEmail(email);
            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                model.addAttribute("staffName", staff.getStaffName());
                model.addAttribute("staffRole", staff.getStaffRole());
                model.addAttribute("staff", staff);
            } else {
                model.addAttribute("staffName", "Staff");
                model.addAttribute("staffRole", null);
            }
        } else {
            model.addAttribute("staffName", "Staff");
            model.addAttribute("staffRole", null);
        }

        Iterable<com.heroku.java.model.Feedback> feedbacks = feedbackRepository.findAll();

        // Join with appointment and customer for display
        List<FeedbackDTO> feedbackList = new ArrayList<>();
        for (com.heroku.java.model.Feedback f : feedbacks) {
            appointmentRepository.findById(f.getAppointmentId()).ifPresent(appt -> {
                customerRepository.findById(appt.getCustId()).ifPresent(cust -> {
                    FeedbackDTO dto = new FeedbackDTO();
                    dto.setFeedbackId(f.getFeedbackId());
                    dto.setCustomerName(cust.getCustName());
                    dto.setRating(f.getRating());
                    dto.setComments(f.getComments());
                    dto.setAppointmentId(f.getAppointmentId());
                    feedbackList.add(dto);
                });
            });
        }

        model.addAttribute("feedbackList", feedbackList);
        return "admin/listFeedback";
    }

    @GetMapping("/admin/register-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerStaffPage() {
        return "admin/registerStaff";
    }

    @PostMapping("/admin/register-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerStaff(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            @RequestParam String role,
            @RequestParam(required = false) String description,
            Model model) {

        // ✅ Get logged-in admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();

        Staff admin = staffRepository.findByStaffEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // ✅ Check email duplicate
        if (staffRepository.findByStaffEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists");
            return "admin/registerStaff";
        }

        Staff staff = new Staff();
        staff.setStaffName(name);
        staff.setStaffEmail(email);
        staff.setStaffPassword(password);
        staff.setStaffPhoneNumber(phone);
        staff.setStaffRole(role);
        staff.setDescription(description);

        // ✅ IMPORTANT
        staff.setAdminId(admin.getStaffId());

        // ✅ Save
        com.heroku.java.service.StaffService staffService =
                new com.heroku.java.service.StaffService(staffRepository);
        staffService.saveStaff(staff);

        return "redirect:/listBarber";
    }

    @GetMapping("/admin/edit-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public String editAdminProfile(jakarta.servlet.http.HttpSession session, Model model) {
        Long staffId = (Long) session.getAttribute("staffId");
        if (staffId == null) {
            return "redirect:/adminLogin";
        }

        staffRepository.findById(staffId).ifPresent(staff -> {
            model.addAttribute("staff", staff);
        });

        return "admin/editProfile";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/update-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateAdminProfile(@org.springframework.web.bind.annotation.RequestParam String name,
            @org.springframework.web.bind.annotation.RequestParam String email,
            @org.springframework.web.bind.annotation.RequestParam String phone,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String password,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String description,
            @org.springframework.web.bind.annotation.RequestParam("image") org.springframework.web.multipart.MultipartFile image,
            jakarta.servlet.http.HttpSession session,
            Model model) {
        Long staffId = (Long) session.getAttribute("staffId");
        if (staffId == null) {
            return "redirect:/adminLogin";
        }

        staffRepository.findById(staffId).ifPresent(staff -> {
            staff.setStaffName(name);
            staff.setStaffEmail(email);
            staff.setStaffPhoneNumber(phone);
            staff.setDescription(description);

            // Handle image upload
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

                    staff.setStaffPicture(fileName);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }

            // Handle password update
            if (password != null && !password.isEmpty()) {
                com.heroku.java.service.StaffService staffService = new com.heroku.java.service.StaffService(
                        staffRepository);
                staff.setStaffPassword(password);
                staffService.saveStaff(staff); // Will hash password
            } else {
                staffRepository.save(staff);
            }

            session.setAttribute("loggedInStaff", staff);
        });

        return "redirect:/admin/profile";
    }

    @GetMapping("/admin/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminProfile(Model model) {
        // Load staff details
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<Staff> staffOpt = staffRepository.findByStaffEmail(email);
            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                model.addAttribute("staff", staff);
                model.addAttribute("staffName", staff.getStaffName());
                model.addAttribute("staffRole", staff.getStaffRole());
            } else {
                model.addAttribute("staffName", "Staff");
                model.addAttribute("staffRole", null);
            }
        } else {
            model.addAttribute("staffName", "Staff");
            model.addAttribute("staffRole", null);
        }

        return "admin/profile";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admin/update-my-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProfile(@org.springframework.web.bind.annotation.RequestParam("staffName") String staffName,
            @org.springframework.web.bind.annotation.RequestParam("staffEmail") String staffEmail,
            @org.springframework.web.bind.annotation.RequestParam("staffPhone") String staffPhone,
            @org.springframework.web.bind.annotation.RequestParam("description") String description,
            @org.springframework.web.bind.annotation.RequestParam(value = "staffPicture", required = false) org.springframework.web.multipart.MultipartFile staffPicture,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            String currentEmail = auth.getName();
            Optional<Staff> staffOpt = staffRepository.findByStaffEmail(currentEmail);
            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                staff.setStaffName(staffName);
                staff.setStaffEmail(staffEmail);
                staff.setStaffPhoneNumber(staffPhone);
                staff.setDescription(description);

                if (staffPicture != null && !staffPicture.isEmpty()) {
                    try {
                        String fileName = System.currentTimeMillis() + "_" + staffPicture.getOriginalFilename();
                        String uploadDir = System.getProperty("user.dir")
                                + "/src/main/resources/static/resources/uploads/";
                        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                        java.nio.file.Files.createDirectories(uploadPath);
                        java.nio.file.Path path = uploadPath.resolve(fileName);
                        java.nio.file.Files.write(path, staffPicture.getBytes());
                        staff.setStaffPicture("/resources/uploads/" + fileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        redirectAttributes.addFlashAttribute("error", "Failed to upload picture: " + e.getMessage());
                        return "redirect:/admin/profile";
                    }
                }

                staffRepository.save(staff);
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Staff not found.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Not authenticated.");
        }

        return "redirect:/admin/profile";
    }

    @PostMapping("/admin/delete-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteStaff(@RequestParam Long staffId,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        // ✅ Prevent admin delete himself
        String email = authentication.getName();
        Staff loggedInAdmin = staffRepository.findByStaffEmail(email).orElse(null);

        if (loggedInAdmin != null && loggedInAdmin.getStaffId().equals(staffId)) {
            redirectAttributes.addFlashAttribute("error",
                    "You cannot delete your own account.");
            return "redirect:/listBarber";
        }

        staffRepository.deleteById(staffId);
        redirectAttributes.addFlashAttribute("success",
                "Staff deleted successfully.");

        return "redirect:/listBarber";
    }

    @GetMapping("/admin/edit-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String editStaff(@RequestParam Long staffId, Model model) {

        staffRepository.findById(staffId).ifPresent(staff -> {
            model.addAttribute("staff", staff);
        });

        return "admin/edit-staff";
    }

    @PostMapping("/admin/update-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStaff(
            @RequestParam Long staffId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String role,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        staffRepository.findById(staffId).ifPresent(staff -> {
            staff.setStaffName(name);
            staff.setStaffEmail(email);
            staff.setStaffPhoneNumber(phone);
            staff.setStaffRole(role);
            staff.setDescription(description);
            staffRepository.save(staff);
        });

        redirectAttributes.addFlashAttribute("success",
                "Staff updated successfully.");

        return "redirect:/listBarber?staffId=" + staffId;
    }

    public String dashboard(Model model) {
        // Get total sales
        java.math.BigDecimal totalSales = paymentRepository.getTotalSales();
        if (totalSales == null) {
            totalSales = java.math.BigDecimal.ZERO;
        }

        // Get total customers
        long totalCustomers = customerRepository.count();

        // Get total appointments
        long totalAppointments = appointmentRepository.count();

        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalAppointments", totalAppointments);

        return "admin/dashboard";
    }

    @GetMapping("/editAppointment")
    @PreAuthorize("hasRole('ADMIN')")
    public String editAppointment(@RequestParam Long appointmentId, Model model) {

        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // customer name
        customerRepository.findById(appt.getCustId())
                .ifPresent(c -> appt.setCustomerName(c.getCustName()));

        model.addAttribute("appointment", appt);

        // barber list
        model.addAttribute("barberList",
                staffRepository.findByStaffRole("BARBER"));

        return "admin/editAppointment";
    }

    @PostMapping("/admin/update-appointment")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateAppointment(
            @RequestParam Long appointmentId,
            @RequestParam(required = false) String appointmentDate,
            @RequestParam(required = false) String appointmentTime,
            @RequestParam String custType,
            @RequestParam Long staffId,
            Authentication authentication) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // ✅ Date (same logic)
        if (appointmentDate != null && !appointmentDate.isEmpty()) {
            a.setAppointmentDate(appointmentDate);
        }

        // ✅ Time (IMPORTANT FIX)
        if (appointmentTime != null && !appointmentTime.isEmpty()) {
            a.setAppointmentTime(appointmentTime);
        }

        a.setCustType(custType);
        a.setBarberId(staffId);

        // audit
        Staff admin = staffRepository.findByStaffEmail(authentication.getName()).orElse(null);
        if (admin != null) {
            a.setUpdatedBy(admin.getStaffId());
            a.setUpdatedAt(java.time.LocalDateTime.now());
        }

        appointmentRepository.save(a);

        return "redirect:/listAppointment?appointmentId=" + appointmentId;
    }

    @PostMapping("/admin/delete-appointment")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAppointment(@RequestParam Long appointmentId,
                                    Authentication authentication) {

        appointmentRepository.findById(appointmentId).ifPresent(a -> {
            Staff admin = staffRepository.findByStaffEmail(authentication.getName()).orElse(null);
            if (admin != null) {
                a.setUpdatedBy(admin.getStaffId());
                a.setUpdatedAt(java.time.LocalDateTime.now());
            }
            appointmentRepository.delete(a);
        });

        return "redirect:/listAppointment";
    }
}
