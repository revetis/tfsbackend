package com.example.apps.payments.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotBlank(message = "Expire month is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expire month must be 01-12")
    private String expireMonth;

    @NotBlank(message = "Expire year is required")
    @Pattern(regexp = "^20[0-9]{2}$", message = "Expire year must be 20XX format")
    private String expireYear;

    @NotBlank(message = "CVC is required")
    @Pattern(regexp = "^[0-9]{3}$", message = "CVC must be 3 digits")
    private String cvc;

    @NotBlank(message = "Buyer email is required")
    @Email(message = "Invalid email format")
    private String buyerEmail;

    @NotBlank(message = "Buyer phone is required")
    private String buyerPhone;

    @NotBlank(message = "Buyer name is required")
    private String buyerName;

    @NotBlank(message = "Buyer surname is required")
    private String buyerSurname;

    @NotBlank(message = "Buyer identity number is required")
    private String buyerIdentityNumber;

    @NotBlank(message = "Buyer address is required")
    private String buyerAddress;

    @NotBlank(message = "Buyer city is required")
    private String buyerCity;

    @NotBlank(message = "Buyer country is required")
    private String buyerCountry;

    private String buyerZipCode;
}
