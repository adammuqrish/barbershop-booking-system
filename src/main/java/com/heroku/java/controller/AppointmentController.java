package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
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
    public String viewAppointments(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
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
            @RequestParam(defaultValue = "0") int page,
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
    public String cancelAppointment(@RequestParam Long appointmentId, HttpSession session) {
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
    public String editAppointment(@RequestParam Long appointmentId, HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
        if (apptOpt.isEmpty() || !apptOpt.get().getCustId().equals(custId)) {
            return "redirect:/view-appointment";
        }
        Appointment appt = apptOpt.get();

        List<com.heroku.java.model.Staff> barbers = bookingService.getAllBarbers();
        Map<String, List<Long>> unavailableBarbersBySlot = bookingService
                .getUnavailableBarbersBySlot(appt.getAppointmentDate(), SLOTS);

        model.addAttribute("appointment", appt);
        model.addAttribute("barbers", barbers);
        model.addAttribute("unavailableBarbersBySlot", unavailableBarbersBySlot);
        model.addAttribute("slots", SLOTS);

        return "customer/edit-appointment";
    }

    @PostMapping("/update-appointment")
    public String updateAppointment(@RequestParam Long appointmentId,
            @RequestParam String date,
            @RequestParam String slot,
            @RequestParam Long barber,
            HttpSession session,
            Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
        if (apptOpt.isEmpty() || !apptOpt.get().getCustId().equals(custId)) {
            return "redirect:/view-appointment";
        }
        Appointment appt = apptOpt.get();

        if (!bookingService.isBarberAvailableForUpdate(barber, date, slot, appointmentId)) {
            List<com.heroku.java.model.Staff> barbers = bookingService.getAllBarbers();
            Map<String, List<Long>> unavailableBarbersBySlot = bookingService.getUnavailableBarbersBySlot(date, SLOTS);

            model.addAttribute("error", "Selected barber is already booked for this slot.");
            model.addAttribute("appointment", appt);
            appt.setAppointmentDate(date);
            model.addAttribute("barbers", barbers);
            model.addAttribute("unavailableBarbersBySlot", unavailableBarbersBySlot);
            model.addAttribute("slots", SLOTS);
            return "customer/edit-appointment";
        }

        appt.setAppointmentDate(date);
        appt.setAppointmentTime(slot);
        appt.setBarberId(barber);

        appointmentRepository.save(appt);

        return "redirect:/view-appointment";
    }
}