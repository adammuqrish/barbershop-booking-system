package com.heroku.java.repository;

import com.heroku.java.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByStaffEmail(String staffEmail);

    List<Staff> findByStaffRole(String staffRole);

    Staff findByStaffPhoneNumber(String staffPhoneNumber);
}
