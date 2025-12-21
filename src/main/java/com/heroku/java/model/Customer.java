package com.heroku.java.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "customers")
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

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

    // Getters and Setters
    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getCustEmail() {
        return custEmail;
    }

    public void setCustEmail(String custEmail) {
        this.custEmail = custEmail;
    }

    public String getCustPassword() {
        return custPassword;
    }

    public void setCustPassword(String custPassword) {
        this.custPassword = custPassword;
    }

    public String getCustPhoneNumber() {
        return custPhoneNumber;
    }

    public void setCustPhoneNumber(String custPhoneNumber) {
        this.custPhoneNumber = custPhoneNumber;
    }

    public String getCustPicture() {
        return custPicture;
    }

    public void setCustPicture(String custPicture) {
        this.custPicture = custPicture;
    }

    public Integer getCustLoyaltyPoints() {
        return custLoyaltyPoints;
    }

    public void setCustLoyaltyPoints(Integer custLoyaltyPoints) {
        this.custLoyaltyPoints = custLoyaltyPoints;
    }
}
