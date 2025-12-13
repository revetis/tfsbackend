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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final IOrderService orderService;
    private final OrderSearchService orderSearchService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderDocument>> searchOrders(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(orderSearchService.searchOrders(
                orderNumber, userId, status, paymentStatus, startDate, endDate));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        OrderDTO cancelledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(cancelledOrder);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<OrderDTO> refundOrder(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        OrderDTO refundedOrder = orderService.refundOrder(id, request);
        return ResponseEntity.ok(refundedOrder);
    }
}
