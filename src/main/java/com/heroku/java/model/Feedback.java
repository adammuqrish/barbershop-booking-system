package com.heroku.java.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "feedbacks")
@Data
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "comments", length = 1000)
    private String comments;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "appointment_id")
    private Long appointmentId;
}
