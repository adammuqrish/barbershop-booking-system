package com.heroku.java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Admin-only endpoints
                        .requestMatchers("/admin/**", "/listCustomer", "/listBarber", "/listAppointment")
                        .hasRole("ADMIN")

                        // Barber and Admin can access
                        .requestMatchers("/barber/**").hasAnyRole("ADMIN", "BARBER")

                        // Public endpoints
                        .requestMatchers("/", "/index", "/register", "/adminLogin", "/auth",
                                "/css/**", "/js/**", "/images/**", "/resources/**")
                        .permitAll()

                        // Customer endpoints (authenticated customers only)
                        .requestMatchers("/profile", "/edit-profile", "/update-profile",
                                "/view-appointment", "/appointment-history", "/booking/**",
                                "/payment", "/processPayment", "/receipt", "/feedback")
                        .authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/adminLogin")
                        .loginProcessingUrl("/staffAuth")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/adminIndex", true)
                        .failureUrl("/adminLogin?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/staffAuth", "/auth") // Allow form submissions
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
