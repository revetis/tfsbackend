package com.example.apps.campaigns.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BestCampaignRequest {
    private BigDecimal orderAmount;
    private List<Long> productIds;
    private List<Long> categoryIds;
}
