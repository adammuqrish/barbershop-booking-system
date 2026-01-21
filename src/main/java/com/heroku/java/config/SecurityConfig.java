package com.heroku.java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth

                                                // --- 1. STATIC FILES ---
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/resources/**",
                                                                "/uploads/**", "/resources/assetsAdmin/**")
                                                .permitAll()

                                                // --- 2. PUBLIC PAGES (INDEX & LOGIN) ---
                                                // TAMBAH "/error" SINI
                                                .requestMatchers("/", "/index", "/register", "/adminLogin", "/error")
                                                .permitAll()

                                                // --- 3. AUTH ENDPOINTS ---
                                                .requestMatchers("/auth", "/staffAuth")
                                                .permitAll()

                                                // --- 4. ADMIN ENDPOINTS ---
                                                .requestMatchers("/admin/**", "/adminIndex")
                                                .hasRole("ADMIN")

                                                // --- 5. BARBER ENDPOINTS ---
                                                .requestMatchers("/barber/**")
                                                .hasRole("BARBER")

                                                // --- 6. CUSTOMER ENDPOINTS ---
                                                .requestMatchers(
                                                                "/profile", "/edit-profile", "/update-profile",
                                                                "/view-appointment", "/appointment-history",
                                                                "/booking/**",
                                                                "/payment", "/processPayment", "/receipt", "/feedback",
                                                                "/cancel-appointment", "/edit-appointment",
                                                                "/update-appointment")
                                                .permitAll()

                                                .anyRequest().authenticated())

                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                                                .logoutSuccessUrl("/index")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/staffAuth", "/auth"));

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}