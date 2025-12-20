package com.heroku.java.model;

import jakarta.persistence.*;

@Entity
@Table(name = "staffs")
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

    // Getters and Setters
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getStaffEmail() { return staffEmail; }
    public void setStaffEmail(String staffEmail) { this.staffEmail = staffEmail; }

    public String getStaffPassword() { return staffPassword; }
    public void setStaffPassword(String staffPassword) { this.staffPassword = staffPassword; }

    public String getStaffPhoneNumber() { return staffPhoneNumber; }
    public void setStaffPhoneNumber(String staffPhoneNumber) { this.staffPhoneNumber = staffPhoneNumber; }

    public String getStaffPicture() { return staffPicture; }
    public void setStaffPicture(String staffPicture) { this.staffPicture = staffPicture; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
}
