package com.example.apps.dashboard.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    // Summary Counts
    private long totalUsers;
    private long activeUsers;
    private long totalProducts;
    private long activeProducts;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long totalShipments;
    private long deliveredShipments;
    private long inTransitShipments;
    private long totalPayments;
    private long successfulPayments;
    private long mainCategoriesCount;
    private long subCategoriesCount;

    // This Month Stats
    private long thisMonthOrders;
    private BigDecimal thisMonthRevenue;

    // Trends (Default last 30 days or parametric)
    private List<DateValuePoint> orderTrend;
    private List<DateValuePoint> revenueTrend;

    // Distributions
    private List<StatusDistributionDTO> orderStatusDistribution;
    private List<StatusDistributionDTO> paymentStatusDistribution;
    private List<StatusDistributionDTO> categoryDistribution;

    // Recent & Popular
    private List<com.example.apps.orders.dtos.OrderDTO> recentOrders;
    private List<TopProductDTO> topProducts;
}
