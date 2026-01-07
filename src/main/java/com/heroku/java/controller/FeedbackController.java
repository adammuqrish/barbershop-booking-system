package com.heroku.java.controller;

import com.heroku.java.model.Feedback;
import com.heroku.java.repository.FeedbackRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ✅ IMPORT NI

import java.util.Optional;

@Controller
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/feedback")
    public String feedbackPage(@RequestParam Long appointmentId, HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custId");
        if (custId == null) return "redirect:/register";

        Optional<Feedback> feedbackOpt = feedbackRepository.findByAppointmentId(appointmentId);
        feedbackOpt.ifPresent(f -> model.addAttribute("existingFeedback", f));
        model.addAttribute("appointmentId", appointmentId);

        return "customer/feedback";
    }

    @PostMapping("/feedback")
    public String submitFeedback(@RequestParam Long appointmentId,
                                 @RequestParam int rating,
                                 @RequestParam String feedback, // Variable name 'feedback' mungkin conflict dengan nama controller, tapi masih ok
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) { // ✅ TAMBAH PARAMETER NI

        Long custId = (Long) session.getAttribute("custId");
        if (custId == null) return "redirect:/register";

        Feedback f = feedbackRepository.findByAppointmentId(appointmentId).orElse(new Feedback());
        f.setAppointmentId(appointmentId);
        f.setRating(rating);
        f.setComments(feedback);
        feedbackRepository.save(f);

        // ✅ 1. SET MESSAGE SUCCESS
        redirectAttributes.addFlashAttribute("success", "Feedback submitted successfully.");

        // ✅ 2. REDIRECT KE HISTORY (DENGAN PARAMETER SOURCE)
        return "redirect:/appointment-history?source=feedback"; 
    }

    @PostMapping("/delete-feedback")
    public String deleteFeedback(@RequestParam Long appointmentId, 
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) { // ✅ TAMBAH PARAMETER NI

        feedbackRepository.findByAppointmentId(appointmentId).ifPresent(feedbackRepository::delete);
        
        // ✅ DELETE JUGA REDIRECT KE HISTORY
        redirectAttributes.addFlashAttribute("success", "Feedback deleted successfully.");
        return "redirect:/appointment-history?source=feedback";
    }
}