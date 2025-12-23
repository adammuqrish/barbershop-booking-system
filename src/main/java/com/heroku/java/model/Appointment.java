package com.heroku.java.model;

import jakarta.persistence.*;

@Entity
@Table(name = "appointments", uniqueConstraints = @UniqueConstraint(name = "unique_barber_slot", columnNames = {
        "barber_id", "appointment_date", "appointment_time" }))
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "cust_id")
    private Long custId;

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
    private String serviceStatus = "pending";

    @Column(name = "cust_book_for")
    private String custBookFor;

    @Transient
    private String customerName;

    @Transient
    private String appointmentBarber;

    @Transient
    private String paymentMethod;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    // Getters and Setters
    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getBarberId() {
        return barberId;
    }

    public void setBarberId(Long barberId) {
        this.barberId = barberId;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Integer getValueLoyalty() {
        return valueLoyalty;
    }

    public void setValueLoyalty(Integer valueLoyalty) {
        this.valueLoyalty = valueLoyalty;
    }

    public String getCustType() {
        return custType;
    }

    public void setCustType(String custType) {
        this.custType = custType;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getCustBookFor() {
        return custBookFor;
    }

    public void setCustBookFor(String custBookFor) {
        this.custBookFor = custBookFor;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAppointmentBarber() {
        return appointmentBarber;
    }

    public void setAppointmentBarber(String appointmentBarber) {
        this.appointmentBarber = appointmentBarber;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
