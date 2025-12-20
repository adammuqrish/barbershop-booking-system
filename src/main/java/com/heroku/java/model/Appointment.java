package com.heroku.java.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "barber_id")
    private Long barberId;

    @Column(name = "appointment_date")
    private String appointmentDate;

    @Column(name = "appointment_time")
    private String appointmentTime;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "value_loyalty")
    private Integer valueLoyalty = 0;

    @Column(name = "cust_type")
    private String custType;

    @Column(name = "service_status")
    private String serviceStatus = "Pending";

    @Column(name = "cust_book_for")
    private String custBookFor;
}
