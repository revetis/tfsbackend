package com.example.apps.auths.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long id;
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
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
