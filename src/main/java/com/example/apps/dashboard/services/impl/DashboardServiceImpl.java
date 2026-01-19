package com.example.apps.dashboard.services.impl;

import com.example.apps.auths.repositories.IUserRepository;
import com.example.apps.dashboard.dtos.DashboardStatsDTO;
import com.example.apps.dashboard.dtos.DateValuePoint;
import com.example.apps.dashboard.dtos.StatusDistributionDTO;
import com.example.apps.dashboard.services.IDashboardService;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.orders.repositories.OrderItemRepository;
import com.example.apps.orders.services.IOrderService;
import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.dashboard.dtos.TopProductDTO;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.shipments.repositories.GeliverShipmentEntityRepository;
import com.example.apps.payments.repositories.PaymentRepository;
import com.example.apps.products.repositories.MainCategoryRepository;
import com.example.apps.products.repositories.SubCategoryRepository;
import com.example.apps.shipments.enums.ShipmentStatus;
import com.example.apps.payments.enums.PaymentStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final IUserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final GeliverShipmentEntityRepository shipmentRepository;
    private final PaymentRepository paymentRepository;
    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final IOrderService orderService;

    @Override
    @Cacheable(value = "dashboardStats", key = "#startDate.toString() + #endDate.toString()")
    public DashboardStatsDTO getDashboardStats(LocalDateTime startDate, LocalDateTime endDate) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 1. Summary Counts
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByEnabledTrue());

        stats.setTotalProducts(productRepository.count());
        stats.setActiveProducts(productRepository.countByEnableTrue());

        stats.setTotalOrders(orderRepository.count());
        BigDecimal totalRevenue = orderRepository.sumTotalAmount();
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        stats.setTotalShipments(shipmentRepository.count());
        stats.setDeliveredShipments(shipmentRepository.countByStatus(ShipmentStatus.DELIVERED));
        stats.setInTransitShipments(shipmentRepository.countByStatus(ShipmentStatus.SHIPPED)); // Assume SHIPPED is in
                                                                                               // transit

        stats.setTotalPayments(paymentRepository.count());
        stats.setSuccessfulPayments(paymentRepository.countByStatus(PaymentStatus.SUCCESS));

        stats.setMainCategoriesCount(mainCategoryRepository.count());
        stats.setSubCategoriesCount(subCategoryRepository.count());

        // 2. This Month Stats
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        stats.setThisMonthOrders(orderRepository.countByCreatedAtAfter(startOfMonth));
        BigDecimal thisMonthRev = orderRepository.sumTotalAmountByCreatedAtAfter(startOfMonth);
        stats.setThisMonthRevenue(thisMonthRev != null ? thisMonthRev : BigDecimal.ZERO);

        // 3. Trends
        stats.setOrderTrend(mapTrendData(orderRepository.findDailyOrderCounts(startDate)));
        stats.setRevenueTrend(mapTrendData(orderRepository.findDailyRevenue(startDate)));

        // 4. Distributions
        stats.setOrderStatusDistribution(orderRepository.findOrderStatusDistribution());
        stats.setPaymentStatusDistribution(orderRepository.findPaymentStatusDistribution());
        stats.setCategoryDistribution(productRepository.findCategoryDistribution());

        // 5. Recent & Popular
        stats.setRecentOrders(orderService.getAll(0, 5, "id", "DESC", null, null, null, null).getContent());
        stats.setTopProducts(orderItemRepository.findTopProducts(org.springframework.data.domain.PageRequest.of(0, 5)));

        return stats;
    }

    private List<DateValuePoint> mapTrendData(List<Object[]> rawData) {
        return rawData.stream().map(row -> {
            LocalDate date = (LocalDate) row[0];
            BigDecimal value = row[1] instanceof Long ? BigDecimal.valueOf((Long) row[1]) : (BigDecimal) row[1];
            return new DateValuePoint(date.toString(), value,
                    date.getDayOfMonth() + " " + date.getMonth().name().substring(0, 3));
        }).collect(Collectors.toList());
    }
}
