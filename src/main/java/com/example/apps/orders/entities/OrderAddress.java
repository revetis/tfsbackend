package com.example.apps.orders.entities;

import com.example.tfs.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddress extends BaseEntity {

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "address_line", nullable = false, length = 1000)
    private String addressLine;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "city_code", nullable = false)
    private String cityCode;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "district_name", nullable = false)
    private String districtName;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "phone_number")
    private String phoneNumber;
}