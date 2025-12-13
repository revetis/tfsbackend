package com.example.apps.orders.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTOIU {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String billingAddress;
    private String notes;
    private String paymentMethod; // For future payment integration

    // Payment card information
    private String cardNumber;
    private String cardHolderName;
    private String expireMonth;
    private String expireYear;
    private String cvc;

    // Buyer information
    private String buyerEmail;
    private String buyerPhone;
    private String buyerName;
    private String buyerSurname;
    private String buyerIdentityNumber;
    private String buyerCity;
    private String buyerCountry;
    private String buyerZipCode;
}
