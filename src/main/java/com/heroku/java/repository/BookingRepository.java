package com.heroku.java.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.heroku.java.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
