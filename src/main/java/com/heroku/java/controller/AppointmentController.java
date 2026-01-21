package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.Staff;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.StaffRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.heroku.java.service.BookingService;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;
    private final BookingService bookingService;

    @Autowired
    public AppointmentController(AppointmentRepository appointmentRepository, StaffRepository staffRepository,
            BookingService bookingService) {
        this.appointmentRepository = appointmentRepository;
        this.staffRepository = staffRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/view-appointment")
    public String viewAppointments(@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        // Get paginated appointments for the customer
        List<String> currentStatuses = java.util.Arrays.asList("pending");

        // Pageable for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentId").descending());
        Page<Appointment> appointmentPage = appointmentRepository.findByCustIdAndServiceStatusIn(custId,
                currentStatuses, pageable);

        // Populate barber name for each appointment
        for (Appointment appt : appointmentPage.getContent()) {
            staffRepository.findById(appt.getBarberId()).ifPresent(s -> appt.setAppointmentBarber(s.getStaffName()));
        }

        model.addAttribute("appointments", appointmentPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", appointmentPage.getTotalPages());
        model.addAttribute("totalItems", appointmentPage.getTotalElements());

        return "customer/view-appointment";
    }

    @GetMapping("/appointment-history")
    public String appointmentHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            HttpSession session,
            Model model) {

        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        // 1. Dapatkan semua appointment customer (Pagination)
        Page<Appointment> appointmentsPage = appointmentRepository.findByCustId(custId, PageRequest.of(page, 5));

        // 2. ✅ FILTER: Hanya appointment yang (Done ATAU Cancelled) DAN (Payment
        // Completed)
        List<Appointment> filteredAppointments = appointmentsPage.getContent().stream()
                .filter(appt -> {
                    boolean isServiceFinished = "done".equalsIgnoreCase(appt.getServiceStatus())
                            || "cancelled".equalsIgnoreCase(appt.getServiceStatus());
                    boolean isPaymentDone = "completed".equalsIgnoreCase(appt.getPaymentStatus());
                    return isServiceFinished && isPaymentDone;
                })
                .collect(Collectors.toList());

        // 3. Populate barber names untuk filtered list
        for (Appointment appt : filteredAppointments) {
            staffRepository.findById(appt.getBarberId())
                    .ifPresent(s -> appt.setAppointmentBarber(s.getStaffName()));
        }

        // 4. Hantar ke HTML
        // Kita guna List biasa (filteredAppointments), bukan Page object, sebab data
        // dah ditapis
        model.addAttribute("appointments", filteredAppointments);

        // Untuk pagination, jika kita tapis data, Page calculation jadi tricky.
        // Untuk simplifikasi, kita hide pagination atau guna list kosong.
        // Di sini kita set total pages = 1 untuk elak error template.
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1); // Pagination disabled/disabled view

        return "customer/appointment-history";
    }

    @PostMapping("/cancel-appointment")
    public String cancelAppointment(@RequestParam(name = "appointmentId") Long appointmentId, HttpSession session) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            if (appointment.getCustId().equals(custId)) {
                appointment.setServiceStatus("cancelled");
                appointmentRepository.save(appointment);
            }
        }

        return "redirect:/view-appointment";
    }

    private final String[] SLOTS = {
            "10:00 am", "10:30 am", "11:00 am", "11:30 am",
            "12:00 pm", "12:30 pm", "1:00 pm", "1:30 pm",
            "2:00 pm", "2:30 pm", "3:00 pm", "3:30 pm",
            "4:00 pm", "4:30 pm", "5:00 pm", "5:30 pm",
            "6:00 pm", "6:30 pm", "7:00 pm", "7:30 pm",
            "8:00 pm", "8:30 pm", "9:00 pm", "9:30 pm"
    };

    @GetMapping("/edit-appointment")
    public String editAppointment(@RequestParam(name = "appointmentId") Long appointmentId, HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
        if (apptOpt.isEmpty() || !apptOpt.get().getCustId().equals(custId)) {
            return "redirect:/view-appointment";
        }
        Appointment appt = apptOpt.get();

        // ✅ TUKAR SINI: Guna Array [] bukan List <>
        String[] slots = {
                "10:00 am", "10:30 am", "11:00 am", "11:30 am",
                "12:00 pm", "12:30 pm", "1:00 pm", "1:30 pm",
                "2:00 pm", "2:30 pm", "3:00 pm", "3:30 pm",
                "4:00 pm", "4:30 pm", "5:00 pm", "5:30 pm",
                "6:00 pm", "6:30 pm", "7:00 pm", "7:30 pm",
                "8:00 pm", "8:30 pm", "9:00 pm", "9:30 pm"
        };

        List<com.heroku.java.model.Staff> barbers = bookingService.getAllBarbers();

        // Sekarang ini tiada error kerana slots adalah String[]
        Map<String, List<Long>> unavailableBarbersBySlot = bookingService
                .getUnavailableBarbersBySlot(appt.getAppointmentDate(), slots);

        model.addAttribute("appointment", appt);
        model.addAttribute("barbers", barbers);
        model.addAttribute("unavailableBarbersBySlot", unavailableBarbersBySlot);
        model.addAttribute("slots", slots);

        return "customer/edit-appointment";
    }

    @PostMapping("/update-appointment")
    public String updateAppointment(@RequestParam(name = "appointmentId") Long appointmentId,
            @RequestParam(name = "appointmentDate") String date,
            @RequestParam(name = "slot") String slot,
            @RequestParam(name = "custType") String custType,
            @RequestParam(name = "staffId") Long barber,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== UPDATE APPOINTMENT ===");
        System.out.println("appointmentId: " + appointmentId);
        System.out.println("date: " + date);
        System.out.println("slot: " + slot);
        System.out.println("custType: " + custType);
        System.out.println("barber: " + barber);
        
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
        if (apptOpt.isEmpty() || !apptOpt.get().getCustId().equals(custId)) {
            return "redirect:/view-appointment";
        }
        Appointment appt = apptOpt.get();

        // --- FIX: SEMAK TARIKH & MASA DAH LEWAT KE BELUM ---
        try {
            String time24 = convertTimeTo24Hour(slot);

            java.time.LocalDateTime appointmentDateTime = java.time.LocalDateTime.parse(
                    date + " " + time24,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            if (appointmentDateTime.isBefore(now)) {
                model.addAttribute("error", "Cannot select a past date or time.");

                // Repopulate data supaya form tak kosong
                appt.setAppointmentDate(date);
                appt.setAppointmentTime(slot);
                appt.setCustType(custType);
                appt.setBarberId(barber);

                List<Staff> barbers = bookingService.getAllBarbers();
                Map<String, List<Long>> unavailableBarbersBySlot = bookingService.getUnavailableBarbersBySlot(date,
                        SLOTS);

                model.addAttribute("appointment", appt);
                model.addAttribute("barbers", barbers);
                model.addAttribute("unavailableBarbersBySlot", unavailableBarbersBySlot);
                model.addAttribute("slots", SLOTS);

                return "customer/edit-appointment";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // -------------------------------------------------

        // --- FIX: SEMAK SLOT DUPLIKAT (OVERLAP) ---
        if (!bookingService.isBarberAvailableForUpdate(barber, date, slot, appointmentId)) {
            model.addAttribute("error", "Selected barber is already booked for this slot.");

            // Repopulate data
            appt.setAppointmentDate(date);
            appt.setAppointmentTime(slot);
            appt.setCustType(custType);
            appt.setBarberId(barber);

            List<Staff> barbers = bookingService.getAllBarbers();
            Map<String, List<Long>> unavailableBarbersBySlot = bookingService.getUnavailableBarbersBySlot(date, SLOTS);

            model.addAttribute("appointment", appt);
            model.addAttribute("barbers", barbers);
            model.addAttribute("unavailableBarbersBySlot", unavailableBarbersBySlot);
            model.addAttribute("slots", SLOTS);

            return "customer/edit-appointment";
        }
        // ------------------------------------------

        // Simpan jika ok
        appt.setAppointmentDate(date);
        appt.setAppointmentTime(slot);
        appt.setCustType(custType);
        appt.setBarberId(barber);

        appointmentRepository.save(appt);

        redirectAttributes.addFlashAttribute("success", "Appointment updated successfully.");
        return "redirect:/view-appointment";
    }

    // Helper method
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
}