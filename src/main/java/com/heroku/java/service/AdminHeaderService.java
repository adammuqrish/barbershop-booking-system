package com.heroku.java.service;

import com.heroku.java.model.Staff;
import com.heroku.java.repository.AppointmentRepository;
import com.heroku.java.repository.StaffRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;

/**
 * Centralises the repeated header/sidebar model population
 * (staffName, staffRole, staff, loggedInStaff) that was copy-pasted
 * across 8 AdminController endpoints.
 */
@Service
public class AdminHeaderService {

    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminHeaderService(StaffRepository staffRepository, AppointmentRepository appointmentRepository) {
        this.staffRepository = staffRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Optional<Staff> getLoggedInStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !"anonymousUser".equals(auth.getName())) {
            return staffRepository.findByStaffEmail(auth.getName());
        }
        return Optional.empty();
    }

    public void populate(Model model) {
        Optional<Staff> opt = getLoggedInStaff();
        if (opt.isPresent()) {
            Staff s = opt.get();
            model.addAttribute("staffName", s.getStaffName());
            model.addAttribute("staffRole", s.getStaffRole());
            model.addAttribute("staff", s);
            model.addAttribute("loggedInStaff", s);
            // Pending bell — admin sees all, barber sees own
            try {
                long pending = "BARBER".equalsIgnoreCase(s.getStaffRole())
                        ? appointmentRepository.countByBarberIdAndServiceStatus(s.getStaffId(), "pending")
                        : appointmentRepository.countByServiceStatus("pending");
                model.addAttribute("pendingCount", pending);
            } catch (Exception e) {
                model.addAttribute("pendingCount", 0L);
            }
        } else {
            model.addAttribute("staffName", "Staff");
            model.addAttribute("staffRole", null);
            model.addAttribute("pendingCount", 0L);
        }
    }

    public void populateWithCurrentPath(Model model, String currentPath) {
        populate(model);
        model.addAttribute("currentPath", currentPath);
    }
}
