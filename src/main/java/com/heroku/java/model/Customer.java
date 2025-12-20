package com.heroku.java.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "customers")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "cust_name")
    private String custName;

    @Column(name = "cust_email", unique = true)
    private String custEmail;

    @Column(name = "cust_password")
    private String custPassword;

    @Column(name = "cust_phone_number")
    private String custPhoneNumber;

    @Column(name = "cust_picture")
    private String custPicture;

    @Column(name = "cust_loyalty_points")
    private Integer custLoyaltyPoints = 0;
}
