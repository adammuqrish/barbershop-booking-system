package com.heroku.java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers.cacheControl(cache -> {}))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/index", "/register", "/adminLogin", "/login",
                                                                 "/error")
                                                .permitAll()
                                                .requestMatchers("/auth/**", "/staffAuth/**").permitAll()
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/resources/**",
                                                                 "/uploads/**", "/assetsAdmin/**",
                                                                 "/resources/assetsAdmin/**",
                                                                 "/webjars/**", "/favicon.ico")
                                                .permitAll()
                                                .requestMatchers("/admin/**", "/adminIndex",
                                                                 "/listCustomer", "/listBarber", "/listAppointment",
                                                                 "/admin/edit-staff", "/admin/update-staff", "/admin/delete-staff",
                                                                 "/api/available-times").hasRole("ADMIN")
                                                .requestMatchers("/barber/**").hasRole("BARBER")
                                                .requestMatchers("/profile", "/edit-profile", "/update-profile",
                                                                 "/view-appointment", "/appointment-history",
                                                                 "/booking/**",
                                                                 "/payment/**", "/processPayment", "/receipt/**",
                                                                 "/feedback/**",
                                                                 "/cancel-appointment", "/edit-appointment",
                                                                 "/update-appointment")
                                                .authenticated()
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                    String uri = request.getRequestURI();
                                                    // Admin / barber protected pages -> admin login, else customer login
                                                    boolean isAdminArea = uri.startsWith("/admin") || uri.equals("/adminIndex")
                                                            || uri.equals("/listCustomer") || uri.equals("/listBarber") || uri.equals("/listAppointment")
                                                            || uri.startsWith("/barber") || uri.equals("/api/available-times");
                                                    if (isAdminArea) {
                                                        response.sendRedirect("/adminLogin");
                                                    } else {
                                                        response.sendRedirect("/login");
                                                    }
                                                }))
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/?logout")
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                    // Always land on public landing page regardless of prior role (customer/admin/barber)
                                                    response.sendRedirect("/?logout");
                                                })
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}