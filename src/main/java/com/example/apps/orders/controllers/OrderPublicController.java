package com.example.apps.orders.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.entities.User;
import com.example.apps.auth.repositories.IUserRepository;
import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.services.IOrderService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/orders")
@RequiredArgsConstructor
public class OrderPublicController {

        private final IOrderService orderService;
        private final IUserRepository userRepository;

        @PostMapping
        public ResponseEntity<ApiTemplate<Void, OrderDTO>> createOrder(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody OrderDTOIU orderDTO, HttpServletRequest servletRequest) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                OrderDTO createdOrder = orderService.createOrder(user.getId(), orderDTO, getClientIp(servletRequest));
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                createdOrder));
        }

        /*
         * Find REAL IP ADDRESS
         */

        private String getClientIp(HttpServletRequest servletRequest) {
                String[] IP_HEADER_CANDIDATES = {
                                "X-Forwarded-For",
                                "Proxy-Client-IP",
                                "WL-Proxy-Client-IP",
                                "HTTP_X_FORWARDED_FOR",
                                "HTTP_X_FORWARDED",
                                "HTTP_X_CLUSTER_CLIENT_IP",
                                "HTTP_CLIENT_IP",
                                "HTTP_FORWARDED_FOR",
                                "HTTP_FORWARDED",
                                "HTTP_VIA",
                                "REMOTE_ADDR"
                };

                for (String header : IP_HEADER_CANDIDATES) {
                        String ip = servletRequest.getHeader(header);

                        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                                return ip.split(",")[0].trim();
                        }
                }

                return servletRequest.getRemoteAddr();
        }

        @GetMapping
        public ResponseEntity<ApiTemplate<Void, List<OrderDTO>>> getMyOrders(
                        @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest servletRequest) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                List<OrderDTO> orders = orderService.getOrdersByUserId(user.getId());
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                orders));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, OrderDTO>> getOrderById(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long id, HttpServletRequest servletRequest) {
                OrderDTO order = orderService.getOrderById(id);
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Ensure user can only view their own orders
                if (!order.getUserId().equals(user.getId())) {
                        throw new RuntimeException("Access denied");
                }

                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                order));
        }

        @PostMapping("/{id}/cancel")
        public ResponseEntity<ApiTemplate<Void, OrderDTO>> cancelOrder(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long id, HttpServletRequest servletRequest) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                OrderDTO cancelledOrder = orderService.cancelOrder(id, user.getId());
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                cancelledOrder));
        }
}
