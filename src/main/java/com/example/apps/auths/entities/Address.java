package com.example.apps.auths.entities;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {

    private String title;
    private String contactName;
    private String fullAddress;
    private String street;
    private String city;
    private String cityCode;
    private String state;
    private String country;
    private String postalCode;
    private String phoneNumber;

    @ManyToOne
    private User user;
}
