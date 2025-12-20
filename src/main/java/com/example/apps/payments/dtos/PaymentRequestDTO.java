package com.example.apps.payments.dtos;

import java.math.BigDecimal;
import java.util.List;

import com.example.apps.payments.enums.Currency;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotBlank(message = "Order number cannot be blank.")
    private String orderNumber;

    @NotBlank(message = "Conversation ID is required for tracking.")
    private String conversationId;

    @NotNull(message = "Please select a valid payment gateway.")
    private String selectedGateway;

    @NotNull(message = "Total price must be specified.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
    private BigDecimal price;

    @NotNull(message = "Paid price cannot be null.")
    private BigDecimal paidPrice;

    @NotNull(message = "Currency must be selected.")
    private Currency currency;

    @NotNull(message = "Installment cannot be null.")
    private List<Integer> installment;

    @Valid
    @NotNull(message = "Buyer information cannot be empty.")
    private BuyerDTO buyer;

    @Valid
    @NotNull(message = "Shipping address is mandatory.")
    private AddressDTO shippingAddress;

    @Valid
    @NotNull(message = "Billing address is mandatory.")
    private AddressDTO billingAddress;

    @Valid
    @NotBlank(message = "Basket ID is required.")
    private String basketId;

    @Valid
    @NotEmpty(message = "Basket must contain at least one item.")
    private List<BasketItemDTO> basketItems;
}