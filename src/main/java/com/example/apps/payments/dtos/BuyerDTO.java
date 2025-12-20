package com.example.apps.payments.dtos;

import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuyerDTO {
    @NotBlank(message = "Buyer ID cannot be blank.")
    private String id;

    @NotBlank(message = "First name is required.")
    private String name;

    @NotBlank(message = "Last name is required.")
    private String surname;

    @Email(message = "Please provide a valid email address.")
    @NotBlank(message = "Email cannot be blank.")
    private String email;

    @NotBlank(message = "Registration address is mandatory.")
    @Size(min = 5, message = "Registration address is too short, please provide more details.")
    private String registrationAddress;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Country is required.")
    private String country;

    @NotBlank(message = "Zip code is required.")
    private String zipCode;

    @NotBlank(message = "IP address is required.")
    private String ip;

    private Date lastLoginDate;

    private Date registrationDate;

    @NotBlank(message = "Identity number is mandatory.")
    @Size(min = 11, max = 11, message = "Identity number must be exactly 11 characters.")
    private String identityNumber;

    @NotBlank(message = "Phone number is required.")
    private String gsmNumber;
}