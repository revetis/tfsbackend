package com.example.apps.payments.entities;

import com.example.apps.payments.enums.AddressType;
import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_addresses")
@Getter
@Setter
@NoArgsConstructor
public class PaymentAddress extends BaseEntity {

    @Column(name = "contact_name")
    private String contactName;
    @Column(name = "city")
    private String city;
    @Column(name = "country")
    private String country;
    @Column(name = "address_line")
    private String addressLine; // iyzico'daki "address" alanı
    @Column(name = "zip_code")
    private String zipCode;

    @Enumerated(EnumType.STRING)
    private AddressType addressType; // SHIPPING, BILLING
}