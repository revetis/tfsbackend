package com.example.apps.orders.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.orders.documents.OrderDocument;
import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.RefundRequest;
import com.example.apps.orders.services.IOrderService;
import com.example.apps.orders.services.search.OrderSearchService;
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final IOrderService orderService;
    private final OrderSearchService orderSearchService;

    @GetMapping
    public ResponseEntity<ApiTemplate<Void, List<OrderDTO>>> getAllOrders(HttpServletRequest servletRequest) {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, OrderDTO>> getOrderById(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, order));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiTemplate<Void, OrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest servletRequest) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, updatedOrder));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiTemplate<Void, List<OrderDocument>>> searchOrders(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest servletRequest) {

        List<OrderDocument> searchResults = orderSearchService.searchOrders(
                orderNumber, userId, status, paymentStatus, startDate, endDate);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, searchResults));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiTemplate<Void, OrderDTO>> cancelOrder(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        OrderDTO cancelledOrder = orderService.cancelOrder(id);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, cancelledOrder));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiTemplate<Void, OrderDTO>> refundOrder(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request,
            HttpServletRequest servletRequest) {
        OrderDTO refundedOrder = orderService.refundOrder(id, request);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, refundedOrder));
    }
}
