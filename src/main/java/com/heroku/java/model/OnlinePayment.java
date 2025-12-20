package com.heroku.java.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "online_payments")
@Data
@EqualsAndHashCode(callSuper = true)
public class OnlinePayment extends Payment {

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_holder_name")
    private String bankHolderName;
}
