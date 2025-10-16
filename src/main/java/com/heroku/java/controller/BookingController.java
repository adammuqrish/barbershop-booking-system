package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.heroku.java.model.Booking;
import com.heroku.java.repository.BookingRepository;

@Controller
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    // Papar semua tempahan
    @GetMapping("/bookings")
    public String listBookings(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "list-bookings"; // pastikan ada list-bookings.html
    }

    // Papar borang edit
    @GetMapping("/edit-bookings/{id}")
    public String editBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking ID: " + id));
        model.addAttribute("booking", booking);
        return "edit-booking"; // inilah file kamu sekarang
    }

    // Simpan perubahan
    @PostMapping("/update/{id}")
    public String updateBooking(@PathVariable Long id, @ModelAttribute("booking") Booking booking) {
        booking.setId(id);
        bookingRepository.save(booking);
        return "redirect:/bookings"; // selepas simpan, kembali ke senarai
    }
}
