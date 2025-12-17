package com.example.apps.shipments.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "geliver_addresses")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeliverAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "geliver_address_id", unique = true)
    private String geliverAddressId; // Geliver address ID

    @Column(name = "short_name")
    private String shortName;

    @Column(nullable = false)
    private String name;

    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, length = 500)
    private String address1;

    @Column(length = 500)
    private String address2;

    @Column(name = "city_code", nullable = false)
    private String cityCode;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column(name = "district_name", nullable = false)
    private String districtName;

    @Column(name = "district_id", nullable = false)
    private Integer districtId;

    @Column(nullable = false)
    private String zip;

    @Column(name = "country_code", nullable = false)
    private String countryCode = "TR";

    @Column(name = "is_default_sender")
    private Boolean isDefaultSender = false;

    @Column(name = "is_default_return")
    private Boolean isDefaultReturn = false;

    @Column(name = "is_recipient")
    private Boolean isRecipient = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
