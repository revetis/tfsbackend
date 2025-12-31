package com.example.apps.orders.controllers;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.services.IOrderService;
import com.example.tfs.maindto.ApiTemplate;
import com.example.tfs.utils.SecurityUtils;

import jakarta.validation.Valid;

/**
 * Efendim, bu sınıf kullanıcıların kendi siparişlerini yönettiği asil
 * katmandır.
 */
@RestController
@RequestMapping("/rest/api/public/orders")
@Validated
public class OrderPublicController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDTOIU orderDTOIU) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_CREATED,
                "/rest/api/private/orders/create",
                null,
                orderService.create(orderDTOIU)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/private/orders/user/" + userId,
                null,
                orderService.getByUserId(userId)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/private/orders/" + orderId,
                null,
                orderService.getById(orderId, userId)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        orderService.cancel(orderId, securityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/private/orders/" + orderId + "/cancel",
                null,
                "Order has been cancelled successfully."));
    }

    @GetMapping("/track")
    public ResponseEntity<?> trackOrder(
            @RequestParam String orderNumber,
            @RequestParam String email) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/public/orders/track",
                null,
                orderService.trackOrder(orderNumber, email)));
    }
}
