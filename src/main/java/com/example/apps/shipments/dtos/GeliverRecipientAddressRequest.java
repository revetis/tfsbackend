package com.example.apps.shipments.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeliverRecipientAddressRequest {

    @NotBlank(message = "Recipient name is required")
    private String name;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+90|0)?5\\d{9}$", message = "Invalid Turkish phone format")
    private String phone;

    @NotBlank(message = "Address line is required")
    private String address1;

    @NotBlank(message = "Country code is required (e.g., TR)")
    private String countryCode;

    @NotBlank(message = "City code is required")
    private String cityCode;

    private String zipCode;

    @NotBlank(message = "District name is required")
    private String districtName;
}