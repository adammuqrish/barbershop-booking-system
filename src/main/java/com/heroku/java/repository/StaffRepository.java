package com.heroku.java.repository;

import com.heroku.java.model.Staff;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.List;

public interface StaffRepository extends CrudRepository<Staff, Long> {
    Optional<Staff> findByStaffEmail(String staffEmail);
    List<Staff> findByStaffRole(String staffRole);
}
