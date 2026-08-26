package com.heroku.java.service;

import com.heroku.java.model.Staff;
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

    public AdminHeaderService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
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
        } else {
            model.addAttribute("staffName", "Staff");
            model.addAttribute("staffRole", null);
        }
    }

    public void populateWithCurrentPath(Model model, String currentPath) {
        populate(model);
        model.addAttribute("currentPath", currentPath);
    }
}
