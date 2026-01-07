package com.heroku.java.controller;

import com.heroku.java.model.Appointment;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Staff;
import com.heroku.java.service.BookingService;
import com.heroku.java.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final CustomerService customerService;

    @Autowired
    public BookingController(BookingService bookingService, CustomerService customerService) {
        this.bookingService = bookingService;
        this.customerService = customerService;
    }

    private final String[] SLOTS = {
            "10:00 am", "10:30 am", "11:00 am", "11:30 am",
            "12:00 pm", "12:30 pm", "1:00 pm", "1:30 pm",
            "2:00 pm", "2:30 pm", "3:00 pm", "3:30 pm",
            "4:00 pm", "4:30 pm", "5:00 pm", "5:30 pm",
            "6:00 pm", "6:30 pm", "7:00 pm", "7:30 pm",
            "8:00 pm", "8:30 pm", "9:00 pm", "9:30 pm"
    };

    @GetMapping("/booking")
    public String bookingPage(@RequestParam(required = false) String date,
            HttpSession session,
            Model model) {

        // ✅ SAFETY: clear any leftover booking state
        session.removeAttribute("lastAppointmentId");

        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        String selectedDate = (date == null || date.isEmpty())
                ? LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                : date;

        LocalDate selected = LocalDate.parse(selectedDate);
        if (selected.isBefore(LocalDate.now())) {
            selectedDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        }

        List<Staff> barbers = bookingService.getAllBarbers();
        Map<String, List<Long>> unavailableBarbersBySlot = bookingService.getUnavailableBarbersBySlot(selectedDate,
                SLOTS);

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("barbers", barbers);
        model.addAttribute("unavailableBarbersBySlot", unavailableBarbersBySlot);
        model.addAttribute("slots", SLOTS);

        return "customer/booking";
    }

    @GetMapping("/booking/unavailable")
    @ResponseBody
    public Map<String, Object> getUnavailable(@RequestParam String date) {
        List<Staff> barbers = bookingService.getAllBarbers();
        Map<String, List<Long>> unavailableBarbersBySlot = bookingService.getUnavailableBarbersBySlot(date, SLOTS);

        Map<String, Object> response = new HashMap<>();
        response.put("unavailableBarbersBySlot", unavailableBarbersBySlot);
        response.put("totalBarbers", barbers.size());
        return response;
    }

    @PostMapping("/booking")
    public String handleBooking(@RequestParam("booking-for") String bookingFor,
            @RequestParam String date,
            @RequestParam String slot,
            @RequestParam String category,
            @RequestParam Long barber,
            HttpSession session,
            Model model) {

        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        // ✅ VALIDASI AVAILABILITY (Boleh ke check dalam service ke? Kalau tak, abaikan
        // buat masa ni)
        // if (!bookingService.isBarberAvailable(barber, date, slot)) {
        // model.addAttribute("error", "Selected barber is already booked for this
        // slot.");
        // return "customer/booking";
        // }

        // ✅ BUAT OBJEK APPOINTMENT
        Appointment appointment = new Appointment();
        appointment.setCustId(custId);
        appointment.setCustBookFor(bookingFor);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(slot);
        appointment.setCustType(category);
        appointment.setBarberId(barber);
        appointment.setServiceStatus("pending");
        appointment.setPaymentStatus("pending");

        // ✅ SIMPAN DALAM SESSION (BUKAN DATABASE)
        session.setAttribute("pendingAppointment", appointment);

        // ✅ BUANG 'lastAppointmentId' sebab kita tak save lagi
        // session.removeAttribute("lastAppointmentId");

        return "redirect:/payment";
    }
}
