package com.example.apps.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long id;
    private String title;
    private String fullAddress;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phoneNumber;
}
