package com.example.apps.auths.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTOIU {

    @Size(max = 255)
    private String title;
    @Size(max = 255, message = "Full address must be less than 255 characters")
    @NotBlank(message = "Full address is required")
    private String fullAddress;
    @NotBlank(message = "Contact name is required")
    private String contactName;
    @Size(max = 255, message = "Street must be less than 255 characters")
    @NotBlank(message = "Street is required")
    private String street;
    @Size(max = 255, message = "City must be less than 255 characters")
    @NotBlank(message = "City is required")
    private String city;
    @Size(max = 20, message = "City code must be less than 20 characters")
    private String cityCode;
    @Size(max = 255, message = "State must be less than 255 characters")
    @NotBlank(message = "State is required")
    private String state;
    @NotBlank(message = "Country is required")
    @Size(max = 255, message = "Country must be less than 255 characters")
    private String country;
    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must be less than 20 characters")
    private String postalCode;
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phoneNumber;
}
