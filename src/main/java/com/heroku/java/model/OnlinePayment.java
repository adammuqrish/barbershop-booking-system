package com.heroku.java.model;

import jakarta.persistence.*;

@Entity
@Table(name = "online_payments")
public class OnlinePayment extends Payment {

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_holder_name")
    private String bankHolderName;

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankHolderName() { return bankHolderName; }
    public void setBankHolderName(String bankHolderName) { this.bankHolderName = bankHolderName; }
}
