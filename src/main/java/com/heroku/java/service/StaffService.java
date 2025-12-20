package com.heroku.java.service;

import com.heroku.java.model.Staff;
import com.heroku.java.repository.StaffRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    @Autowired
    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public Optional<Staff> login(String email, String password) {
        Optional<Staff> staffOpt = staffRepository.findByStaffEmail(email);
        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();
            String storedPassword = staff.getStaffPassword();
            
            try {
                if (storedPassword != null && (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2y$") || storedPassword.startsWith("$2b$"))) {
                    if (BCrypt.checkpw(password, storedPassword)) {
                        return Optional.of(staff);
                    }
                } else {
                    if (password.equals(storedPassword)) {
                        staff.setStaffPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                        staffRepository.save(staff);
                        return Optional.of(staff);
                    }
                }
            } catch (Exception e) {
                if (password.equals(storedPassword)) {
                    staff.setStaffPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                    staffRepository.save(staff);
                    return Optional.of(staff);
                }
            }
        }
        return Optional.empty();
    }

    public Staff saveStaff(Staff staff) {
        if (staff.getStaffPassword().length() < 30) { // Simple check for hashing
             staff.setStaffPassword(BCrypt.hashpw(staff.getStaffPassword(), BCrypt.gensalt()));
        }
        return staffRepository.save(staff);
    }
}
