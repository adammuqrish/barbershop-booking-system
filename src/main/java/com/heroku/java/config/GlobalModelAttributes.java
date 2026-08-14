package com.heroku.java.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

/**
 * Exposes editable barbershop contact details to every Thymeleaf template so the
 * owner can change them via application.properties instead of editing HTML.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${barbershop.phone:0127865132}")
    private String phone;

    @Value("${barbershop.opening-hours:TUESDAY - SUNDAY (10 a.m - 10 p.m)}")
    private String openingHours;

    @Value("${barbershop.email:hugibarbershop@gmail.com}")
    private String email;

    @Value("${barbershop.address:LOT-A-G9, Palm Garden, Bandar Baru Klang, 41150 Klang, Selangor.}")
    private String address;

    @ModelAttribute("barbershop")
    public Map<String, String> barbershop() {
        return Map.of(
                "phone", phone,
                "openingHours", openingHours,
                "email", email,
                "address", address);
    }
}
