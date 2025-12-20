package com.example.apps.payments.entities;

import java.util.Date;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_buyers")
@Getter
@Setter
@NoArgsConstructor
public class PaymentBuyer extends BaseEntity {

    private String buyerId; // Kayıtlı ise User ID, değilse "GUEST"
    private String name;
    private String surname;
    private String identityNumber;
    private String email;
    private String gsmNumber;
    private String registrationAddress;
    private Date lastLoginDate;
    private Date registrationDate;
    private String city;
    private String country;
    private String zipCode;
    private String ip;
}