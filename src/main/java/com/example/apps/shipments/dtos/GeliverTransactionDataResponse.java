package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GeliverTransactionDataResponse {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String amount;
    private String totalAmount;
    private String currency;
    private String offerID;

    // Efendim, işte asıl sevkiyat detayları burada
    private GeliverTransactionShipmentResponse shipment;

    private String description;
    private boolean isPayed;
    private String payedVia; // Örn: BALANCE
    private String transactionType;
    private String invoiceID;
    private String newBalance;
}