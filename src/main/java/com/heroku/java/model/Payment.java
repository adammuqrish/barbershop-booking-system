package com.heroku.java.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "payment_date")
    private LocalDate paymentDate = LocalDate.now();

    @Column(name = "payment_amount")
    private Double paymentAmount;

    @Column(name = "appointment_id")
    private Long appointmentId;
}
