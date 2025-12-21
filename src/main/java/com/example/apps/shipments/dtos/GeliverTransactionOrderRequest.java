package com.example.apps.shipments.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GeliverTransactionOrderRequest {

    @NotBlank(message = "Source code is required")
    private String sourceCode = "API";

    private String sourceIdentifier;

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotNull(message = "Total amount is required")
    @Min(value = 0, message = "Total amount cannot be negative")
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency is required")
    private String totalAmountCurrency = "TL";
}