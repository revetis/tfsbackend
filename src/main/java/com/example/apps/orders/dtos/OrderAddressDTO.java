package com.example.apps.orders.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderAddressDTO {
    @NotBlank(message = "Contact name cannot be blank.")
    private String contactName;

    @NotBlank(message = "Address line is mandatory.")
    private String addressLine;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "City code is required.")
    private String cityCode;

    @NotBlank(message = "District name is required.")
    private String districtName;

    @NotBlank(message = "Country is required.")
    private String country;

    @NotBlank(message = "Zip code is required.")
    private String zipCode;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;
}
