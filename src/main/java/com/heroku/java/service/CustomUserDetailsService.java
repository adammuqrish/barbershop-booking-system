package com.heroku.java.service;

import com.heroku.java.model.Staff;
import com.heroku.java.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;

    @Autowired
    public CustomUserDetailsService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Staff> staffOpt = staffRepository.findByStaffEmail(email);

        if (staffOpt.isEmpty()) {
            throw new UsernameNotFoundException("Staff not found with email: " + email);
        }

        Staff staff = staffOpt.get();

        // Map staff role to Spring Security authority
        List<GrantedAuthority> authorities = new ArrayList<>();
        String role = staff.getStaffRole();

        if (role != null) {
            // Add ROLE_ prefix as required by Spring Security
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        }

        return User.builder()
                .username(staff.getStaffEmail())
                .password(staff.getStaffPassword())
                .authorities(authorities)
                .build();
    }
}
