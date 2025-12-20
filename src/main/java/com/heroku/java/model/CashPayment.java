package com.heroku.java.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cashes")
public class CashPayment extends Payment {

    @Column(name = "cash_receive")
    private Double cashReceive;

    public Double getCashReceive() { return cashReceive; }
    public void setCashReceive(Double cashReceive) { this.cashReceive = cashReceive; }
}
