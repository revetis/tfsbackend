package com.example.apps.payments.dtos;

import com.example.apps.payments.enums.AddressType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    @NotBlank(message = "Contact name cannot be blank.")
    private String contactName;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Country is required.")
    private String country;

    @NotBlank(message = "Address line is mandatory.")
    @Size(min = 5, message = "Address line is too short, please provide more details.")
    private String addressLine;

    @NotBlank(message = "Zip code is required.")
    private String zipCode;

    @NotNull(message = "Address type is required.")
    @Enumerated(EnumType.STRING)
    private AddressType addressType;
}