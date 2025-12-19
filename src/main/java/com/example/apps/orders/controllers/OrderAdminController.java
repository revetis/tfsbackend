package com.example.apps.orders.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.apps.orders.services.IOrderService;
import com.example.tfs.maindto.ApiTemplate;

import io.netty.handler.codec.http.HttpResponseStatus;

@RestController
@RequestMapping("/rest/api/admin/orders")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {

    @Autowired
    private IOrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/admin/orders/all",
                null,
                orderService.getAll()));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<?> returnOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/admin/orders/" + orderId + "/return",
                null,
                orderService.returnOrder(orderId)));
    }
}