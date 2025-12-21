package com.example.apps.shipments.dtos;

import lombok.Data;
import java.util.List;
import java.time.OffsetDateTime;

@Data
public class GeliverOffersResponse {
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String length;
    private String width;
    private String height;
    private String weight;
    private GeliverOfferDetailResponse cheapest;
    private GeliverOfferDetailResponse fastest;
    private List<GeliverOfferDetailResponse> list;
    private int percentageCompleted;
    private int totalOffersRequested;
    private int totalOffersCompleted;
}