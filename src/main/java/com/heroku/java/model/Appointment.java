package com.heroku.java.model;

import jakarta.persistence.*;

@Entity
@Table(name = "appointments", schema = "public")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "staff_id")
    private Integer staffId;

    @Column(name = "barber_id")
    private Integer barberId;

    @Column(name = "appointment_date")
    private String appointmentDate;

    @Column(name = "appointment_time")
    private String appointmentTime;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "value_loyalty")
    private Integer valueLoyalty;

    @Column(name = "cust_type")
    private String custType;

    @Column(name = "service_status")
    private String serviceStatus;

    @Column(name = "cust_book_for")
    private String custBookFor;

    // Getters and Setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public Integer getBarberId() { return barberId; }
    public void setBarberId(Integer barberId) { this.barberId = barberId; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Integer getValueLoyalty() { return valueLoyalty; }
    public void setValueLoyalty(Integer valueLoyalty) { this.valueLoyalty = valueLoyalty; }

    public String getCustType() { return custType; }
    public void setCustType(String custType) { this.custType = custType; }

    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String serviceStatus) { this.serviceStatus = serviceStatus; }

    public String getCustBookFor() { return custBookFor; }
    public void setCustBookFor(String custBookFor) { this.custBookFor = custBookFor; }
}
