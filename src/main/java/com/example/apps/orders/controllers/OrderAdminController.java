package com.example.apps.orders.controllers;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.example.apps.orders.services.IOrderService;
import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.dtos.OrderStatusUpdateDTO;
import com.example.tfs.maindto.ApiTemplate;

@RestController
@RequestMapping("/rest/api/admin/orders")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {

    @Autowired
    private IOrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/admin/orders/all",
                null,
                orderService.getAll(page, size, sort, direction, q, status, paymentStatus, userId)));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDTOIU orderDTOIU) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_CREATED,
                "/rest/api/admin/orders",
                null,
                orderService.create(orderDTOIU)));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<?> returnOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/admin/orders/" + orderId + "/return",
                null,
                orderService.returnOrder(orderId, null)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        orderService.cancel(orderId, null);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/admin/orders/" + orderId + "/cancel",
                null,
                "Order cancelled successfully"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                org.apache.hc.core5.http.HttpStatus.SC_OK,
                "/rest/api/admin/orders/" + orderId,
                null,
                orderService.getOrderByIdAdmin(orderId)));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateDTO dto) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/admin/orders/" + orderId,
                null,
                orderService.updateStatus(orderId, dto.getStatus())));
    }
}
