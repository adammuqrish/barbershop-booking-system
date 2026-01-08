package com.heroku.java.controller;

import com.heroku.java.model.Feedback;
import com.heroku.java.model.Appointment; // ✅ IMPORT NI
import com.heroku.java.repository.FeedbackRepository;
import com.heroku.java.repository.AppointmentRepository; // ✅ IMPORT NI
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
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final AppointmentRepository appointmentRepository; // ✅ INJECT NI

    @Autowired
    public FeedbackController(FeedbackRepository feedbackRepository, 
                            AppointmentRepository appointmentRepository) { // ✅ CONSTRUCTOR UPDATE
        this.feedbackRepository = feedbackRepository;
        this.appointmentRepository = appointmentRepository;
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
                                 @RequestParam String feedback,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Long custId = (Long) session.getAttribute("custId");
        if (custId == null) return "redirect:/register";

        // ✅ 1. SEMAK STATUS APPOINTMENT
        Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
        if (apptOpt.isEmpty()) {
            return "redirect:/appointment-history"; // Appointment tak wujud
        }

        Appointment appt = apptOpt.get();

        // ✅ 2. VALIDASI STATUS: Service mesti 'DONE', Payment mesti 'COMPLETED'
        boolean isServiceDone = "done".equalsIgnoreCase(appt.getServiceStatus());
        boolean isPaymentCompleted = "completed".equalsIgnoreCase(appt.getPaymentStatus());

        if (!isServiceDone || !isPaymentCompleted) {
            redirectAttributes.addFlashAttribute("error", 
                "You can only give feedback after the service is done and payment is completed.");
            return "redirect:/appointment-history?source=feedback";
        }

        // ✅ 3. VALIDASI RATING
        if (rating <= 0) {
            redirectAttributes.addFlashAttribute("error", "Please select a star rating.");
            return "redirect:/feedback?appointmentId=" + appointmentId;
        }

        // ✅ 4. SIMPAN FEEDBACK
        Feedback f = feedbackRepository.findByAppointmentId(appointmentId).orElse(new Feedback());
        f.setAppointmentId(appointmentId);
        f.setRating(rating);
        f.setComments(feedback);
        feedbackRepository.save(f);

        redirectAttributes.addFlashAttribute("success", "Feedback submitted successfully.");
        return "redirect:/appointment-history?source=feedback";
    }

    @PostMapping("/delete-feedback")
    public String deleteFeedback(@RequestParam Long appointmentId, 
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        // Delete tak perlu check status, allow je delete
        feedbackRepository.findByAppointmentId(appointmentId).ifPresent(feedbackRepository::delete);
        
        redirectAttributes.addFlashAttribute("success", "Feedback deleted successfully.");
        return "redirect:/appointment-history?source=feedback";
    }
}