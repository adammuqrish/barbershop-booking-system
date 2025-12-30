package com.heroku.java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth

                // Admin only
                .requestMatchers("/admin/**", "/listCustomer", "/listBarber", "/listAppointment")
                .hasRole("ADMIN")

                // Barber + Admin
                .requestMatchers("/barber/**")
                .hasAnyRole("ADMIN", "BARBER")

                // Public
                .requestMatchers("/", "/index", "/register", "/adminLogin", "/auth",
                        "/css/**", "/js/**", "/images/**", "/resources/**")
                .permitAll()

                // Customer pages (handled by session)
                .requestMatchers("/profile", "/edit-profile", "/update-profile",
                        "/view-appointment", "/appointment-history", "/booking/**",
                        "/payment", "/processPayment", "/receipt", "/feedback")
                .permitAll()   // ✅ BIARKAN SESSION HANDLE

                .anyRequest().authenticated()
                )
                .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/index")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                )
                .csrf(csrf -> csrf
                .ignoringRequestMatchers("/staffAuth", "/auth")
                );

        return http.build();
        }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
