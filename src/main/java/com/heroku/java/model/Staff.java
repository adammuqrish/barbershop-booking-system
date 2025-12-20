package com.heroku.java.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "staffs")
@Data
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "staff_name")
    private String staffName;

    @Column(name = "staff_email", unique = true)
    private String staffEmail;

    @Column(name = "staff_password")
    private String staffPassword;

    @Column(name = "staff_phone_number")
    private String staffPhoneNumber;

    @Column(name = "staff_picture")
    private String staffPicture;

    @Column(name = "description")
    private String description;

    @Column(name = "staff_role")
    private String staffRole;

    @Column(name = "admin_id")
    private Long adminId;
}
