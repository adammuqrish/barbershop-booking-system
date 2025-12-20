package com.heroku.java.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "cashes")
@Data
@EqualsAndHashCode(callSuper = true)
public class CashPayment extends Payment {

    @Column(name = "cash_receive")
    private Double cashReceive;
}
