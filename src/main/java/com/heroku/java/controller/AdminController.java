package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.CashPayment;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Feedback;
import com.heroku.java.model.Staff;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.CustomerRepository;
import com.heroku.java.repository.StaffRepository;
import com.heroku.java.service.BookingService;
import com.heroku.java.service.StaffService;

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
    private final BookingService bookingService;
    private final StaffService staffService;

    @Autowired
    public AdminController(CustomerRepository customerRepository,
            StaffRepository staffRepository,
            AppointmentRepository appointmentRepository,
            PaymentRepository paymentRepository,
            com.heroku.java.repository.FeedbackRepository feedbackRepository,
            BookingService bookingService,
            StaffService staffService) {
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
        this.feedbackRepository = feedbackRepository;
        this.bookingService = bookingService;
        this.staffService = staffService;
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
    public String barberCustomerList(
            @RequestParam(required = false) Long custId, // ✅ TAMBAH PARAMETER NI
            Model model) {

        Staff barber = getLoggedInStaff();
        if (barber == null)
            return "redirect:/adminLogin";

        // Set info user untuk header
        model.addAttribute("staffName", barber.getStaffName());
        model.addAttribute("staffRole", barber.getStaffRole());
        model.addAttribute("staff", barber);

        // 1. Dapatkan senarai customer yang assign kepada barber ini
        List<Customer> customers = customerRepository.findCustomersByStaffId(barber.getStaffId());
        model.addAttribute("customerList", customers);

        // 2. Logik View Details (Jika ada custId dihantar)
        if (custId != null) {
            Optional<Customer> customerOpt = customerRepository.findById(custId);

            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();

                // ✅ SECURITY CHECK: Pastikan customer ni benar-benar assign kepada barber ini
                // Kita check senarai customers tadi. Kalau customer wujud dalam senarai tu,
                // baru layak tengok.
                boolean isAssignedToBarber = customers.stream()
                        .anyMatch(c -> c.getCustId().equals(custId));

                if (isAssignedToBarber) {
                    model.addAttribute("customer", customer);
                } else {
                    // Jika Barber cuba 'hack' URL untuk tengok customer lain
                    model.addAttribute("error", "You are not authorized to view this customer.");
                }
            } else {
                model.addAttribute("error", "Customer not found.");
            }
        }

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

        // --- LOGIK BARU: BARBER HANYA LIHAT MILIK SENDIRI ---

        // 1. Dapatkan SEMUA payment (atau kita boleh terus filter appointment,
        // tapi struktur anda join melalui Payment. Kita guna Payment)
        Iterable<Payment> barberPayments = paymentRepository.findAllByStaffId(barber.getStaffId());

        List<TransactionDTO> transactions = new ArrayList<>();
        for (Payment p : barberPayments) {
            // Kita sudah filter di repository (findAllByStaffId),
            // jadi kesemua 'p' di sini adalah untuk barber ini sahaja.
            // Tiada perlu check lagi di sini.

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

        String oldEmail = barber.getStaffEmail();
        String oldPhone = barber.getStaffPhoneNumber();
        Long currentStaffId = barber.getStaffId();

        // --- VALIDASI DUPLIKAT PHONE (BUG FIX) ---

        // Semak hanya jika nombor telefon ditukar
        if (!staffPhone.equals(oldPhone)) {
            Staff existingStaff = staffRepository.findByStaffPhoneNumber(staffPhone);

            // Jika nombor wujud DAN ia bukan milik diri sendiri
            if (existingStaff != null && !existingStaff.getStaffId().equals(currentStaffId)) {
                redirectAttributes.addFlashAttribute("error", "Phone number already exist");
                return "redirect:/barber/profile";
            }
        }
        // ------------------------------------------

        // Update data
        barber.setStaffName(staffName);
        barber.setStaffEmail(staffEmail);
        barber.setStaffPhoneNumber(staffPhone);
        barber.setDescription(description);

        // Handle Image
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

        // Save DB
        staffRepository.save(barber);

        // Check Email
        if (!oldEmail.equalsIgnoreCase(staffEmail)) {
            SecurityContextHolder.clearContext();
            redirectAttributes.addFlashAttribute("forceLogoutModal", true);
            return "redirect:/adminLogin";
        } else {
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

    // 8. ADMIN INDEX
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

    // 9. ADMIN CUSTOMER LIST
    @GetMapping("/listCustomer")
    @PreAuthorize("hasRole('ADMIN')")
    public String listCustomers(
            @org.springframework.web.bind.annotation.RequestParam(name = "custId", required = false) Long custId,
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

    // 10. ADMIN BARBER LIST
    @GetMapping("/listBarber")
    @PreAuthorize("hasRole('ADMIN')")
    public String listBarber(
            @RequestParam(name = "staffId", required = false) Long staffId,
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

    // 11. ADMIN APPOINTMENT LIST
    @GetMapping("/listAppointment")
    @PreAuthorize("hasRole('ADMIN')")
    public String listAppointments(
            @RequestParam(name = "appointmentId", required = false) Long appointmentId,
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

    // 12. API AVAILABLE TIMES
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

    // 13. API UPDATE SERVICE STATUS
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

    // 14. ADMIN LIST TRANSACTIONS
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

    // 15. ADMIN LIST FEEDBACK
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

    // 16. ADMIN REGISTER STAFF
    @GetMapping("/admin/register-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerStaffPage(Model model) {
        // Get logged-in admin info for header
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            staffRepository.findByStaffEmail(auth.getName()).ifPresent(admin -> {
                model.addAttribute("staffName", admin.getStaffName());
                model.addAttribute("staffRole", admin.getStaffRole());
                model.addAttribute("staff", admin);
            });
        }
        return "admin/registerStaff";
    }

    @PostMapping("/admin/register-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerStaff(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String phone,
            @RequestParam String role,
            @RequestParam(required = false) String description,
            Model model) {

        // 1. Dapatkan Admin yang login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();

        Staff admin = staffRepository.findByStaffEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // 2. Set data asas untuk header (supaya header tak rosak)
        model.addAttribute("staffName", admin.getStaffName());
        model.addAttribute("staffRole", admin.getStaffRole());
        model.addAttribute("staff", admin);

        // 3. Dapatkan senarai barber & Map (Wajib ada untuk jadual!)
        List<Staff> barberList = staffRepository.findAll();
        model.addAttribute("barberList", barberList);

        Map<Long, String> adminNameMap = new HashMap<>();
        for (Staff s : barberList) {
            if (s.getAdminId() != null) {
                staffRepository.findById(s.getAdminId())
                        .ifPresent(a -> adminNameMap.put(s.getStaffId(), a.getStaffName()));
            }
        }
        model.addAttribute("adminNameMap", adminNameMap);

        // --- BAHAGIAN VALIDASI (Seperti sebelum ini) ---

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "Please fill this input");
            return "admin/listBarber";
        }

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Please fill out this field");
            return "admin/listBarber";
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            model.addAttribute("error", "Please use correct format");
            return "admin/listBarber";
        }

        if (staffRepository.findByStaffEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exist");
            return "admin/listBarber";
        }

        // --- BAHAGIAN PHONE ---
        if (phone == null || phone.trim().isEmpty()) {
            model.addAttribute("error", "Please fill this input");
            return "admin/listBarber";
        }

        // (Pastikan ada method ini dalam Repository)
        if (staffRepository.findByStaffPhoneNumber(phone) != null) {
            model.addAttribute("error", "Phone number already exist");
            return "admin/listBarber";
        }
        // <-- Pastikan ada kurungan penutup di sini

        // --- BAHAGIAN PASSWORD ---
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Please fill this input");
            return "admin/listBarber";
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            model.addAttribute("error", "Please fill this input");
            return "admin/listBarber";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Password do not match");
            return "admin/listBarber";
        }

        if (role == null || role.trim().isEmpty()) {
            model.addAttribute("error", "Please select a role");
            return "admin/listBarber";
        }

        // --- SIMPAN DATA (Seperti sebelum ini) ---

        Staff staff = new Staff();
        staff.setStaffName(name);
        staff.setStaffEmail(email);
        staff.setStaffPassword(password);
        staff.setStaffPhoneNumber(phone);
        staff.setStaffRole(role);
        staff.setDescription(description);
        staff.setAdminId(admin.getStaffId());

        staffService.saveStaff(staff);

        return "redirect:/listBarber";
    }

    // 17. ADMIN EDIT PROFILE
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

    // 18. ADMIN PROFILE
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

    // 19. ADMIN UPDATE HIS/HER PROFILE
    @PostMapping("/admin/update-my-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProfile(
            @RequestParam("staffName") String staffName,
            @RequestParam("staffEmail") String staffEmail,
            @RequestParam("staffPhone") String staffPhone,
            @RequestParam("description") String description,
            @RequestParam(value = "staffPicture", required = false) MultipartFile staffPicture,
            @RequestParam(value = "staffPassword", required = false) String staffPassword,
            RedirectAttributes redirectAttributes) {

        // 1. Dapatkan user yang sedang login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();

        // 2. Dapatkan data staff dari DB
        Optional<Staff> staffOpt = staffRepository.findByStaffEmail(currentEmail);

        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();
            Long currentStaffId = staff.getStaffId();
            String oldEmail = staff.getStaffEmail();
            String oldPhone = staff.getStaffPhoneNumber();

            // --- VALIDASI DUPLIKAT PHONE (BUG FIX #004_11) ---

            // Semak hanya jika nombor telefon ditukar
            if (!staffPhone.equals(oldPhone)) {
                Staff existingStaff = staffRepository.findByStaffPhoneNumber(staffPhone);

                // Jika nombor wujud DAN ia bukan milik diri sendiri
                if (existingStaff != null && !existingStaff.getStaffId().equals(currentStaffId)) {
                    redirectAttributes.addFlashAttribute("error", "Phone number already exist");
                    return "redirect:/admin/profile";
                }
            }
            // -------------------------------------------------

            // 3. Update data asas
            staff.setStaffName(staffName);
            staff.setStaffEmail(staffEmail);
            staff.setStaffPhoneNumber(staffPhone);
            staff.setDescription(description);

            // 4. Handle Password Update
            if (staffPassword != null && !staffPassword.isEmpty()) {
                com.heroku.java.service.StaffService staffService = new com.heroku.java.service.StaffService(
                        staffRepository);
                staff.setStaffPassword(staffPassword);
                staffService.saveStaff(staff);
            }

            // 5. Handle Image
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

            // 6. Simpan ke Database
            if (staffPassword == null || staffPassword.isEmpty()) {
                staffRepository.save(staff);
            }

            // 7. Logik Logout jika email berubah
            if (!oldEmail.equalsIgnoreCase(staffEmail)) {
                SecurityContextHolder.clearContext();
                redirectAttributes.addFlashAttribute("forceLogoutModal", true);
                return "redirect:/adminLogin";
            } else {
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
                return "redirect:/admin/profile";
            }

        } else {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/profile";
        }
    }

    // 20. ADMIN DELETE STAFF
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

    // 21. ADMIN EDIT STAFF
    @GetMapping("/admin/edit-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String editStaff(@RequestParam(required = false) Long staffId, Model model) {
        
        // Get logged-in admin info for header
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            staffRepository.findByStaffEmail(auth.getName()).ifPresent(admin -> {
                model.addAttribute("staffName", admin.getStaffName());
                model.addAttribute("staffRole", admin.getStaffRole());
            });
        }

        if (staffId != null) {
            staffRepository.findById(staffId).ifPresentOrElse(
                staff -> model.addAttribute("staff", staff),
                () -> model.addAttribute("error", "Staff not found.")
            );
        } else {
            model.addAttribute("error", "Invalid staff ID provided.");
        }

        return "admin/edit-staff";
    }

    // 22. ADMIN UPDATE STAFF
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

    // 23. ADMIN EDIT APPOINTMENT
    // @GetMapping("/admin/edit-appointment")
    // @PreAuthorize("hasRole('ADMIN')")
    // public String editAppointment(@RequestParam Long appointmentId, Model model)
    // {

    // Appointment appt = appointmentRepository.findById(appointmentId)
    // .orElseThrow(() -> new RuntimeException("Appointment not found"));

    // // customer name
    // customerRepository.findById(appt.getCustId())
    // .ifPresent(c -> appt.setCustomerName(c.getCustName()));

    // model.addAttribute("appointment", appt);

    // // barber list
    // model.addAttribute("barberList",
    // staffRepository.findByStaffRole("BARBER"));

    // // --- TAMBAHAN: Hantar data slot yang tak available ---

    // // ✅ BAHARUI: Guna kurungan segi empat
    // String[] slots = new String[] { "10:00 am", "10:30 am", "11:00 am", "11:30
    // am", "12:00 pm", "12:30 pm",
    // "01:00 pm", "01:30 pm", "02:00 pm", "02:30 pm", "03:00 pm", "03:30 pm",
    // "04:00 pm", "04:30 pm", "05:00 pm", "05:30 pm", "06:00 pm", "06:30 pm",
    // "07:00 pm", "07:30 pm", "08:00 pm", "08:30 pm", "09:00 pm", "09:30 pm" };

    // String currentDate = appt.getAppointmentDate();

    // // PANGGIL SERVICE (Guna Arrays.asList sebab parameter service adalah List)
    // Map<String, List<Long>> unavailableBarbersBySlot =
    // bookingService.getUnavailableBarbersBySlot(currentDate,
    // slots);

    // model.addAttribute("unavailableBarbersBySlot", unavailableBarbersBySlot);
    // model.addAttribute("currentSlot", appt.getAppointmentTime());
    // // --------------------------------------------------

    // return "admin/editAppointment";
    // }

    // 24. ADMIN UPDATE APPOINTMENT
    @PostMapping("/admin/update-appointment")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateAppointment(
            @RequestParam Long appointmentId,
            @RequestParam(required = false) String appointmentDate,
            @RequestParam(required = false) String appointmentTime,
            @RequestParam(name = "barberId", required = false) Long staffId,
            RedirectAttributes redirectAttributes,
            Authentication authentication,
            Model model) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // --- FIX 1: VALIDASI TARIKH & MASA ---
        if (appointmentDate != null && !appointmentDate.isEmpty() &&
                appointmentTime != null && !appointmentTime.isEmpty()) {

            try {
                String time24 = convertTimeTo24Hour(appointmentTime);
                java.time.LocalDateTime newDateTime = java.time.LocalDateTime.parse(
                        appointmentDate + " " + time24,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                if (newDateTime.isBefore(now)) {
                    // ❌ ERROR: Past Date/Time
                    // Kita REDIRECT dengan mesej error (bukan return page)
                    redirectAttributes.addFlashAttribute("error", "Cannot select a past date or time.");
                    return "redirect:/listAppointment?appointmentId=" + appointmentId;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // -----------------------------------------

        // --- FIX 2: VALIDASI SLOT DUPLIKAT ---
        // Kita perlu check availability
        if (staffId != null && appointmentDate != null && appointmentTime != null) {
            // Guna logic BookingService atau manual check
            if (!bookingService.isBarberAvailableForUpdate(staffId, appointmentDate, appointmentTime, appointmentId)) {
                // ❌ ERROR: Slot Full
                // Kita REDIRECT dengan mesej error (bukan return page)
                redirectAttributes.addFlashAttribute("error", "Selected barber is already booked for this slot.");
                return "redirect:/listAppointment?appointmentId=" + appointmentId;
            }
        }
        // -----------------------------------------

        // --- BERJAYA: UPDATE & REDIRECT ---
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

        // Jika berjaya, hantar success message
        redirectAttributes.addFlashAttribute("success", "Appointment updated successfully.");
        return "redirect:/listAppointment?appointmentId=" + appointmentId;
    }

    // Helper method
    // Convert time to 24-hour format
    private String convertTimeTo24Hour(String slot) {
        String[] parts = slot.split(" ");
        String time = parts[0];
        String ampm = parts[1];

        String[] hm = time.split(":");
        int hour = Integer.parseInt(hm[0]);

        if (ampm.equalsIgnoreCase("pm") && hour != 12) {
            hour += 12;
        } else if (ampm.equalsIgnoreCase("am") && hour == 12) {
            hour = 0;
        }

        return String.format("%02d:%02d", hour, Integer.parseInt(hm[1]));
    }

    // 25. ADMIN DELETE APPOINTMENT
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
