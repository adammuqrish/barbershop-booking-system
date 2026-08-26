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
    public String handleBooking(@RequestParam(value = "booking-for", required = false) String bookingFor,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "slot", required = false) String slot,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "barber", required = false) Long barber,
            HttpSession session,
            Model model) {

        Long custId = (Long) session.getAttribute("custId");
        if (custId == null)
            return "redirect:/register";

        // ✅ SERVER-SIDE VALIDATION: all required fields presence checked here
        String missingField = null;
        if (bookingFor == null || bookingFor.trim().isEmpty()) missingField = "Booking for";
        else if (date == null || date.trim().isEmpty()) missingField = "date";
        else if (slot == null || slot.trim().isEmpty()) missingField = "time slot";
        else if (barber == null) missingField = "barber";
        else if (category == null || category.trim().isEmpty()) missingField = "category";

        if (missingField != null) {
            model.addAttribute("barbers", bookingService.getAllBarbers());
            model.addAttribute("slots", SLOTS);
            model.addAttribute("selectedDate", date != null ? date : LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            model.addAttribute("unavailableBarbersBySlot",
                    bookingService.getUnavailableBarbersBySlot(date != null ? date : LocalDate.now().format(DateTimeFormatter.ISO_DATE), SLOTS));
            model.addAttribute("bookingError",
                    "Please complete the entire form before confirming your booking. Missing: " + missingField + ".");
            return "customer/booking";
        }

        // ✅ SERVER-SIDE VALIDATION: barber must be free for this exact date+slot
        if (!bookingService.isBarberAvailable(barber, date, slot)) {
            model.addAttribute("barbers", bookingService.getAllBarbers());
            model.addAttribute("slots", SLOTS);
            model.addAttribute("selectedDate", date);
            model.addAttribute("unavailableBarbersBySlot",
                    bookingService.getUnavailableBarbersBySlot(date, SLOTS));
            model.addAttribute("bookingError",
                    "That barber is already booked for " + slot + " on " + date
                            + ". Please pick another barber or time.");
            return "customer/booking";
        }

        // ✅ SERVER-SIDE VALIDATION: no bookings in the past / same-day cutoff
        LocalDate chosen;
        try {
            chosen = LocalDate.parse(date);
        } catch (Exception e) {
            model.addAttribute("bookingError", "Invalid date selected. Please pick a valid date.");
            return "customer/booking";
        }
        if (chosen.isBefore(LocalDate.now())) {
            return "redirect:/booking";
        }

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
