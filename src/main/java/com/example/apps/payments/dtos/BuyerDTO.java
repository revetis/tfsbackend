package com.example.apps.payments.dtos;

import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private String registrationAddress;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Country is required.")
    private String country;

    @NotBlank(message = "Zip code is required.")
    private String zipCode;

    private String ip;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginDate;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registrationDate;

    @NotBlank(message = "Identity number is mandatory.")
    private String identityNumber;

    @NotBlank(message = "Phone number is required.")
    private String gsmNumber;
}