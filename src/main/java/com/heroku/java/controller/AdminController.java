package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.CashPayment;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Feedback;
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
import org.springframework.web.bind.annotation.ResponseBody;
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

    // Helper method untuk dapatkan Staff yang sedang login
    private Staff getLoggedInStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            return staffRepository.findByStaffEmail(auth.getName()).orElse(null);
        }
        return null;
    }

    // ==========================================
    // SECTION: BARBER ENDPOINTS
    // ==========================================

    // 1. BARBER DASHBOARD
    @GetMapping("/barber/dashboard")
    public String barberDashboard(Model model) {
        Staff barber = getLoggedInStaff();
        if (barber == null)
            return "redirect:/adminLogin";

        // Set user info untuk header (sama macam admin)
        model.addAttribute("staffName", barber.getStaffName());
        model.addAttribute("staffRole", barber.getStaffRole());
        model.addAttribute("staff", barber);

        // 1. Total Sales (Hanya untuk appointment barber ini)
        // Anggap method 'getTotalSalesByStaffId' wujud dalam PaymentRepository (lihat
        // bahagian Repository)
        java.math.BigDecimal totalSales = paymentRepository.getTotalSalesByStaffId(barber.getStaffId());
        if (totalSales == null)
            totalSales = java.math.BigDecimal.ZERO;
        model.addAttribute("totalSales", totalSales);

        // 2. Total Customers (Hanya customer yang ada appointment dengan barber ini)
        // Anda mungkin ada method khas atau kita guna logic manual
        // Untuk mudah, kita guna countDistinctCustomerByStaffId jika ada, atau 0 dulu
        long totalCustomers = appointmentRepository.countDistinctCustomersByStaffId(barber.getStaffId());
        model.addAttribute("customerCount", totalCustomers);

        // 3. Total Appointments
        long totalAppointments = appointmentRepository.countByBarberId(barber.getStaffId());
        model.addAttribute("totalAppointments", totalAppointments);

        // 4. Sales Graph Data (Filter ikut barber)
        Map<String, Double> salesByDay = new HashMap<>();
        salesByDay.put("SUNDAY", 0.0);
        salesByDay.put("MONDAY", 0.0);
        salesByDay.put("TUESDAY", 0.0);
        salesByDay.put("WEDNESDAY", 0.0);
        salesByDay.put("THURSDAY", 0.0);
        salesByDay.put("FRIDAY", 0.0);
        salesByDay.put("SATURDAY", 0.0);

        // Dapatkan payment untuk barber ini sahaja
        Iterable<Payment> barberPayments = paymentRepository.findAllByStaffId(barber.getStaffId());

        for (Payment p : barberPayments) {
            if (p.getPaymentDate() != null) {
                String dayName = p.getPaymentDate().getDayOfWeek().toString();
                Double currentAmount = salesByDay.get(dayName);
                if (currentAmount == null)
                    currentAmount = 0.0;
                salesByDay.put(dayName, currentAmount + p.getAmount().doubleValue());
            }
        }
        model.addAttribute("salesByDay", salesByDay);

        // 5. List Customer (Hanya yang assign kepada barber ini)
        // Kita guna kaedah findCustomersByStaffId jika ada, atau filter dari
        // appointment
        List<Customer> assignedCustomers = customerRepository.findCustomersByStaffId(barber.getStaffId());
        model.addAttribute("customerList", assignedCustomers);

        return "admin/adminIndex"; // Guna template yang sama
    }

    // 2. BARBER CUSTOMER LIST
    @GetMapping("/barber/customers")
    public String barberCustomerList(Model model) {
        Staff barber = getLoggedInStaff();
        if (barber == null)
            return "redirect:/adminLogin";

        model.addAttribute("staffName", barber.getStaffName());
        model.addAttribute("staffRole", barber.getStaffRole());
        model.addAttribute("staff", barber);

        // Filter customers
        List<Customer> customers = customerRepository.findCustomersByStaffId(barber.getStaffId());
        model.addAttribute("customerList", customers);

        return "admin/listCustomer"; // Guna template yang sama
    }

    // 3. BARBER APPOINTMENT LIST
    @GetMapping("/barber/appointments")
    public String barberAppointmentList(Model model) {
        Staff barber = getLoggedInStaff();
        if (barber == null)
            return "redirect:/adminLogin";

        model.addAttribute("staffName", barber.getStaffName());
        model.addAttribute("staffRole", barber.getStaffRole());
        model.addAttribute("staff", barber);
        model.addAttribute("loggedInStaff", barber); // Untuk kegunaan template

        // Filter appointments
        List<Appointment> appointments = appointmentRepository.findByBarberId(barber.getStaffId());

        for (Appointment a : appointments) {
            customerRepository.findById(a.getCustId())
                    .ifPresent(c -> a.setCustomerName(c.getCustName()));

            // Set payment info
            paymentRepository.findByAppointmentId(a.getAppointmentId())
                    .ifPresent(p -> {
                        if ("online-banking".equalsIgnoreCase(p.getPaymentMethod())) {
                            a.setPaymentMethod("ONLINE");
                            a.setPaymentStatus("completed");
                        } else {
                            a.setPaymentMethod("CASH");
                            a.setPaymentStatus(a.getPaymentStatus());
                        }
                    });
        }
        model.addAttribute("appointmentList", appointments);

        // Barber tak perlu dropdown list barber untuk edit (sebab dia tak boleh edit)
        // tapi kita biarkan kosong atau null supaya butang edit tak keluar dalam
        // template

        return "admin/listAppointment";
    }

    // 4. BARBER TRANSACTION LIST
    @GetMapping("/barber/transactions")
    public String barberTransactionList(Model model) {
        Staff barber = getLoggedInStaff();
        if (barber == null)
            return "redirect:/adminLogin";

        model.addAttribute("staffName", barber.getStaffName());
        model.addAttribute("staffRole", barber.getStaffRole());
        model.addAttribute("staff", barber);

        // Filter transactions (payment) berdasarkan staffId barber
        Iterable<Payment> payments = paymentRepository.findAllByStaffId(barber.getStaffId());

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

    // 5. BARBER FEEDBACK LIST
    @GetMapping("/barber/feedbacks")
    public String barberFeedbackList(Model model) {
        Staff barber = getLoggedInStaff();
        if (barber == null)
            return "redirect:/adminLogin";

        model.addAttribute("staffName", barber.getStaffName());
        model.addAttribute("staffRole", barber.getStaffRole());
        model.addAttribute("staff", barber);

        // Filter feedbacks (melalui appointment -> barberId)
        List<Feedback> feedbacks = feedbackRepository.findByStaffId(barber.getStaffId());

        List<FeedbackDTO> feedbackList = new ArrayList<>();
        for (Feedback f : feedbacks) {
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

    // 6. BARBER PROFILE
    @GetMapping("/barber/profile")
    public String barberProfile(Model model) {
        Staff barber = getLoggedInStaff();
        if (barber == null)
            return "redirect:/adminLogin";

        model.addAttribute("staffName", barber.getStaffName());
        model.addAttribute("staffRole", barber.getStaffRole());
        model.addAttribute("staff", barber);

        return "admin/profile"; // Guna template admin/profile
    }

    @PostMapping("/barber/update-profile")
    public String updateBarberProfile(
            @RequestParam("staffName") String staffName,
            @RequestParam("staffEmail") String staffEmail,
            @RequestParam("staffPhone") String staffPhone,
            @RequestParam("description") String description,
            @RequestParam(value = "staffPicture", required = false) MultipartFile staffPicture,
            RedirectAttributes redirectAttributes) {

        Staff barber = getLoggedInStaff();
        if (barber == null)
            return "redirect:/adminLogin";

        // 1. Simpan email lama
        String oldEmail = barber.getStaffEmail();

        // 2. Update data
        barber.setStaffName(staffName);
        barber.setStaffEmail(staffEmail);
        barber.setStaffPhoneNumber(staffPhone);
        barber.setDescription(description);

        // 3. Handle Image
        if (staffPicture != null && !staffPicture.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + staffPicture.getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/resources/uploads/";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath))
                    java.nio.file.Files.createDirectories(uploadPath);
                java.nio.file.Files.write(uploadPath.resolve(fileName), staffPicture.getBytes());
                barber.setStaffPicture(fileName);
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Failed to upload picture");
                return "redirect:/barber/profile";
            }
        }

        // 4. Save DB
        staffRepository.save(barber);

        // 5. ✅ CHECK EMAIL
        if (!oldEmail.equalsIgnoreCase(staffEmail)) {
            // Email berubah -> Logout
            SecurityContextHolder.clearContext();
            redirectAttributes.addFlashAttribute("forceLogoutModal", true);
            return "redirect:/adminLogin";
        } else {
            // Email tak berubah -> Stay
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
            return "redirect:/barber/profile";
        }
    }

    // 7. BARBER LIST (Barber boleh tengok barber lain tapi tak boleh register)
    @GetMapping("/barber/list-barber")
    public String barberListBarber(Model model) {
        Staff barber = getLoggedInStaff();
        if (barber == null)
            return "redirect:/adminLogin";

        model.addAttribute("staffName", barber.getStaffName());
        model.addAttribute("staffRole", barber.getStaffRole());
        model.addAttribute("staff", barber);

        model.addAttribute("barberList", staffRepository.findAll());

        // Map admin name sama macam admin
        Map<Long, String> adminNameMap = new HashMap<>();
        for (Staff s : staffRepository.findAll()) {
            if (s.getAdminId() != null) {
                staffRepository.findById(s.getAdminId())
                        .ifPresent(admin -> adminNameMap.put(s.getStaffId(), admin.getStaffName()));
            }
        }
        model.addAttribute("adminNameMap", adminNameMap);

        return "admin/listBarber";
    }

    @GetMapping("/adminIndex")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminIndex(Model model) {
        // Load staff from authentication
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

        // 1. Total Sales
        java.math.BigDecimal totalSales = paymentRepository.getTotalSales();
        if (totalSales == null) {
            totalSales = java.math.BigDecimal.ZERO;
        }
        model.addAttribute("totalSales", totalSales);

        // 2. Total Counts
        model.addAttribute("customerCount", customerRepository.count());
        model.addAttribute("totalAppointments", appointmentRepository.count());

        // 3. Sales by Day (KIRANAN DATA SEBENAR)
        Map<String, Double> salesByDay = new HashMap<>();

        // Inisialisasi semua hari dengan 0.0 supaya tak null
        salesByDay.put("SUNDAY", 0.0);
        salesByDay.put("MONDAY", 0.0);
        salesByDay.put("TUESDAY", 0.0);
        salesByDay.put("WEDNESDAY", 0.0);
        salesByDay.put("THURSDAY", 0.0);
        salesByDay.put("FRIDAY", 0.0);
        salesByDay.put("SATURDAY", 0.0);

        // Dapatkan SEMUA payment
        Iterable<Payment> allPayments = paymentRepository.findAll();

        // Loop setiap payment untuk kumpul ikut hari
        for (Payment p : allPayments) {
            if (p.getPaymentDate() != null) {
                // Dapatkan hari dari date (Contoh: MONDAY)
                String dayName = p.getPaymentDate().getDayOfWeek().toString();

                // Tambah jumlah ke hari tersebut
                Double currentAmount = salesByDay.get(dayName);
                if (currentAmount == null)
                    currentAmount = 0.0;

                salesByDay.put(dayName, currentAmount + p.getAmount().doubleValue());
            }
        }

        model.addAttribute("salesByDay", salesByDay);

        // Optional: Hantar list customer untuk jadual di bawah (jika ada)
        model.addAttribute("customerList", customerRepository.findAll());

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
                model.addAttribute("staff", admin);
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
                        .ifPresent(admin -> adminNameMap.put(s.getStaffId(), admin.getStaffName()));
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
                    model.addAttribute("staff", admin);
                    model.addAttribute("loggedInStaff", admin);
                });

        // ✅ appointment list
        Iterable<Appointment> appointments = appointmentRepository.findAllSortedByDateAndStatus();

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

        // ✅ barber dropdown (PENYELESAIAN MASALAH)
        // Kita tak guna findByStaffRole("BARBER") sebab ianya terlalu ketat.
        // Kita guna findAll() dan filter sendiri.
        List<Staff> allStaff = staffRepository.findAll();
        List<Staff> barbers = new ArrayList<>();

        for (Staff s : allStaff) {
            // Kita masukkan sesiapa sahaja yang role dia ADMIN atau BARBER
            // (Selagi dia staff, dia boleh jadi barber)
            if ("ADMIN".equalsIgnoreCase(s.getStaffRole()) || "BARBER".equalsIgnoreCase(s.getStaffRole())) {
                barbers.add(s);
            }
        }

        model.addAttribute("barberList", barbers);

        return "admin/listAppointment";
    }

    @GetMapping("/api/available-times")
    @ResponseBody
    public List<String> getAvailableTimes(
            @RequestParam String date,
            @RequestParam(required = false) Long barberId, // Tambah required = false
            @RequestParam(required = false) Long excludeAppointmentId) {

        List<Appointment> bookedAppointments;

        // LOGIK BARU:
        // Jika barberId dihantar -> Hanya check availability barber tersebut
        // Jika barberId NULL -> Ambil SEMUA booking untuk tarikh tu (lebih ketat)
        // Atau -> Anda boleh return SEMUA masa jika barberId null (lebih bebas).

        // Opsi A (Disarankan untuk Edit): Jika tak pilih barber, anggap semua masa
        // free,
        // KECUALI masa yang dah book untuk appointment sendiri (exclude logic handle
        // ni).
        // TAPI ini boleh menyebabkan conflict jika user pilih masa yang sama dengan
        // barber lain.

        // Opsi B (Selamat): Jika tak pilih barber, check semua barber lain.

        if (barberId != null) {
            bookedAppointments = appointmentRepository.findByBarberIdAndAppointmentDate(barberId, date);
        } else {
            // Jika barber belum dipilih, kita tak boleh check conflict barber lain sebab
            // kita tak tahu siapa.
            // Jadi kita return semua masa, tapi KENA pastikan excludeAppointmentId
            // berjalan.
            // Cara paling mudah: Return semua masa generateTimeSlots() terus,
            // tapi excludeAppointmentId akan check masa sendiri.

            // Namun, untuk mengelakkan conflict (double booking), kita boleh:
            // Cari SEMUA appointment pada tarikh tersebut (tanpa mengira barber).
            bookedAppointments = appointmentRepository.findByAppointmentDate(date);
        }

        List<String> allSlots = generateTimeSlots();
        List<String> availableTimes = new ArrayList<>();

        for (String slot : allSlots) {
            boolean isTaken = false;
            for (Appointment app : bookedAppointments) {

                // Jika barberId ada, kita check STRICT ikut barber tu.
                // Jika barberId NULL, kita check agak longgar atau strict ikut semua.
                if (barberId != null && !app.getBarberId().equals(barberId)) {
                    continue; // Skip, barber lain takpe
                }

                if (app.getAppointmentTime().equalsIgnoreCase(slot)) {
                    // Exclude self
                    if (excludeAppointmentId != null && app.getAppointmentId().equals(excludeAppointmentId)) {
                        // Allow self
                    } else {
                        isTaken = true;
                    }
                }
            }
            if (!isTaken) {
                availableTimes.add(slot);
            }
        }

        return availableTimes;
    }

    // Helper method untuk generate masa (10:00 AM - 9:30 PM)
    private List<String> generateTimeSlots() {
        List<String> slots = new ArrayList<>();
        String[] times = { "10:00 am", "10:30 am", "11:00 am", "11:30 am", "12:00 pm", "12:30 pm",
                "01:00 pm", "01:30 pm", "02:00 pm", "02:30 pm", "03:00 pm", "03:30 pm",
                "04:00 pm", "04:30 pm", "05:00 pm", "05:30 pm", "06:00 pm", "06:30 pm",
                "07:00 pm", "07:30 pm", "08:00 pm", "08:30 pm", "09:00 pm", "09:30 pm" };

        for (String time : times) {
            slots.add(time);
        }
        return slots;
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
        com.heroku.java.service.StaffService staffService = new com.heroku.java.service.StaffService(staffRepository);
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

    @PostMapping("/admin/update-my-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProfile(
            @RequestParam("staffName") String staffName,
            @RequestParam("staffEmail") String staffEmail,
            @RequestParam("staffPhone") String staffPhone,
            @RequestParam("description") String description,
            @RequestParam(value = "staffPicture", required = false) MultipartFile staffPicture,
            @RequestParam(value = "staffPassword", required = false) String staffPassword, // ✅ TAMBAH NI
            RedirectAttributes redirectAttributes) {

        // 1. Dapatkan user yang sedang login (Current Auth)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();

        // 2. Dapatkan data staff dari DB
        Optional<Staff> staffOpt = staffRepository.findByStaffEmail(currentEmail);

        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();

            // 3. Simpan email LAMA untuk comparison
            String oldEmail = staff.getStaffEmail();

            // 4. Update data asas
            staff.setStaffName(staffName);
            staff.setStaffEmail(staffEmail);
            staff.setStaffPhoneNumber(staffPhone);
            staff.setDescription(description);

            // 5. Handle Password Update (Dari Method 1)
            if (staffPassword != null && !staffPassword.isEmpty()) {
                // Guna StaffService untuk hash password
                com.heroku.java.service.StaffService staffService = new com.heroku.java.service.StaffService(
                        staffRepository);
                staff.setStaffPassword(staffPassword); // Service akan hashkan dia
                staffService.saveStaff(staff);
            }

            // 6. Handle Image (Guna path yang lebih stabil)
            if (staffPicture != null && !staffPicture.isEmpty()) {
                try {
                    String fileName = System.currentTimeMillis() + "_" + staffPicture.getOriginalFilename();
                    String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/resources/uploads/";
                    java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                    if (!java.nio.file.Files.exists(uploadPath)) {
                        java.nio.file.Files.createDirectories(uploadPath);
                    }
                    java.nio.file.Path path = uploadPath.resolve(fileName);
                    java.nio.file.Files.write(path, staffPicture.getBytes());
                    staff.setStaffPicture(fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Failed to upload picture");
                    return "redirect:/admin/profile";
                }
            }

            // 7. Simpan ke Database (Jika password tak diupdate, save biasa)
            if (staffPassword == null || staffPassword.isEmpty()) {
                staffRepository.save(staff);
            }

            // 8. LOGIK PENENTUAN EMAIL
            if (!oldEmail.equalsIgnoreCase(staffEmail)) {
                // Email berubah -> Force Logout
                SecurityContextHolder.clearContext();
                redirectAttributes.addFlashAttribute("forceLogoutModal", true);
                return "redirect:/adminLogin";
            } else {
                // Email tak berubah -> Refresh
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
                return "redirect:/admin/profile";
            }

        } else {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/profile";
        }
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
            @RequestParam(name = "barberId", required = false) Long staffId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointmentDate != null && !appointmentDate.isEmpty()) {
            a.setAppointmentDate(appointmentDate);
        }

        if (appointmentTime != null && !appointmentTime.isEmpty()) {
            a.setAppointmentTime(appointmentTime);
        }

        if (staffId != null) {
            a.setBarberId(staffId);
        }

        Staff admin = staffRepository.findByStaffEmail(authentication.getName()).orElse(null);
        if (admin != null) {
            a.setUpdatedBy(admin.getStaffId());
            a.setUpdatedAt(java.time.LocalDateTime.now());
        }

        appointmentRepository.save(a);

        redirectAttributes.addFlashAttribute("success", "Appointment updated successfully.");
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
