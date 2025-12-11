package com.example.apps.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    private String fullAddress;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phoneNumber;
}
