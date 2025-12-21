package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GeliverOfferDetailResponse {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String amount;
    private String currency;
    private String totalAmount;
    private String providerCode;
    private String providerServiceCode;
    private String averageEstimatedTimeHumanReadible;
    private boolean isAccepted;
    private boolean isGlobal;
    private String integrationType;
}