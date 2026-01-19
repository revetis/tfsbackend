package com.example.apps.orders.controllers;

import com.example.apps.auths.securities.JWTGenerator;
import com.example.apps.notifications.services.IEmailService;
import com.example.apps.notifications.services.IN8NService;
import com.example.apps.orders.dtos.CreateReturnRequestDTO;
import com.example.apps.orders.dtos.GuestThreadRequest;
import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.ReturnRequestResponseDTO;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.exceptions.OrderException;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.orders.services.IOrderService;
import com.example.apps.orders.services.IReturnService;
import com.example.apps.orders.services.impl.OrderService;
import com.example.tfs.ApplicationProperties;
import com.example.tfs.maindto.ApiTemplate;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/api/public/guest-orders")
@RequiredArgsConstructor
@Slf4j
public class GuestOrderController {

    private final OrderRepository orderRepository; // Using repository directly for lookup or via Service
    private final IReturnService returnService;
    private final IOrderService orderService; // Need this if we want to use convertToDTO logic
    private final JWTGenerator jwtGenerator;
    private final IN8NService n8nService;
    private final ApplicationProperties applicationProperties;

    @PostMapping("/request-return-token")
    public ResponseEntity<ApiTemplate<Object, String>> requestReturnToken(@RequestBody GuestThreadRequest request) {
        log.info("Requesting guest return token for Order: {}, Email: {}", request.getOrderNumber(),
                request.getEmail());

        Order order = orderRepository.findByOrderNumber(request.getOrderNumber())
                .orElseThrow(() -> new OrderException("Sipariş bulunamadı."));

        if (!order.getCustomerEmail().equalsIgnoreCase(request.getEmail())) {
            // Security: Don't reveal mismatch details too explicitly, but for Guest UX we
            // might say "Bilgiler eşleşmedi".
            throw new OrderException("Sipariş numarası veya e-posta adresi hatalı.");
        }

        // Generate Token
        String token = jwtGenerator.generateGuestActionToken(order.getOrderNumber(), request.getEmail(),
                "GUEST_RETURN");

        // Construct Link
        // Ensure no double slash
        String baseUrl = applicationProperties.getFRONTEND_URL().replaceAll("/$", "");
        String link = baseUrl + "/guest/return?token=" + token;

        // Construct Logo URL
        // Used URL from properties (backend url) + standard favicon path or img path
        // User requested: http://localhost:8080/favicon.ico
        String logoUrl = applicationProperties.getURL() + "/favicon.ico";

        // Trigger N8N Workflow instead of EmailService
        n8nService.sendGuestReturnRequest(order.getOrderNumber(), order.getCustomerName(), request.getEmail(), link,
                logoUrl);

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(), "/request-return-token",
                null, "İade talebi bağlantısı e-posta adresinize gönderildi ve N8N tetiklendi."));
    }

    @GetMapping("/validate-return-token")
    public ResponseEntity<ApiTemplate<Object, OrderDTO>> validateReturnToken(@RequestParam String token) {
        Claims claims = jwtGenerator.validateGuestToken(token);
        String orderNumber = claims.getSubject();
        String action = claims.get("action", String.class);

        if (!"GUEST_RETURN".equals(action)) {
            throw new OrderException("Geçersiz işlem isteği.");
        }

        // Re-use logic to get OrderDTO
        // We know OrderService has logic but it usually takes ID.
        // We need DTO.
        // OrderService.convertToDTO is private usually or inside Service.

        // Let's manually fetch and map or expose a method.
        // Assuming OrderService has a way to get by OrderNumber returning DTO?
        // Checking OrderService... default doesn't seem to have getByOrderNumber
        // returning DTO.

        // Let's fetch Entity and map it manually for now to avoid breaking changes,
        // OR better: cast orderService to OrderService impl and access mapping if
        // public? No bad practice.
        // Since we are in Controller, we should rely on Service.
        // But OrderService interface usually has getById.

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException("Order not found"));

        // Using OrderService to map if possible, but mapToDTO is usually
        // private/protected.
        // Hack: Create a simple mapper here or rely on BeanUtils + setting items
        // manually.
        // Wait, OrderService has convertToDTO?

        // Let's use a simplified logical flow:
        // We need basic order details + items for the return form.
        // I will implement a quick mapping here or rely on ModelMapper if project has
        // it.
        // Project uses manual mapping usually.

        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);

        // Items
        // OrderItemService.getByOrderId(order.getId()) returns List<OrderItemDTO>
        // Use injected OrderItemService if possible.
        // Hard to inject private bean from here.

        // Simpler: Just rely on order.getOrderItems() and map them manually.
        // Or if I can't access OrderItemDTO easily...

        // Actually, OrderDTO returned by OrderService.getById is what we want.
        // So:
        OrderDTO fullDto = null;
        try {
            // Use getByOrderNumber as it is available in IOrderService and appropriate here
            fullDto = orderService.getByOrderNumber(orderNumber, null);

        } catch (Exception e) {
            throw new OrderException("Error fetching order details" + e.getMessage());
        }

        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(), "/validate-return-token", null, fullDto));
    }

    @PostMapping("/create-return")
    public ResponseEntity<ApiTemplate<Object, ReturnRequestResponseDTO>> createReturn(
            @RequestBody com.example.apps.orders.dtos.CreateGuestReturnRequest request) {
        Claims claims = jwtGenerator.validateGuestToken(request.getToken());
        String orderNumber = claims.getSubject();
        String email = claims.get("email", String.class);

        // Map simplified Guest DTO to full CreateReturnRequestDTO
        CreateReturnRequestDTO serviceRequest = new CreateReturnRequestDTO();
        // Look up Order ID
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        serviceRequest.setOrderId(order.getId());

        serviceRequest.setReturnReason(request.getReturnReason());
        serviceRequest.setDescription(request.getDescription());

        // Map items
        java.util.List<com.example.apps.orders.dtos.ReturnItemRequestDTO> items = request.getItems().stream().map(i -> {
            com.example.apps.orders.dtos.ReturnItemRequestDTO itemDto = new com.example.apps.orders.dtos.ReturnItemRequestDTO();
            itemDto.setOrderItemId(i.getOrderItemId());
            itemDto.setQuantity(i.getQuantity());
            return itemDto;
        }).collect(java.util.stream.Collectors.toList());

        serviceRequest.setItems(items);

        ReturnRequestResponseDTO response = returnService.createGuestReturnRequest(orderNumber, email, serviceRequest);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(), "/create-return", null, response));
    }

    @PostMapping("/cancel-order")
    public ResponseEntity<ApiTemplate<Object, String>> cancelOrder(@RequestParam String token) {
        Claims claims = jwtGenerator.validateGuestToken(token);
        String orderNumber = claims.getSubject();
        String action = claims.get("action", String.class);

        if (!"GUEST_RETURN".equals(action)) {
            throw new OrderException("Geçersiz işlem isteği.");
        }

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException("Sipariş bulunamadı."));

        if (order.getStatus() == com.example.apps.orders.enums.OrderStatus.DELIVERED ||
                order.getStatus() == com.example.apps.orders.enums.OrderStatus.SHIPPED) {
            throw new OrderException(
                    "Kargolanmış veya teslim edilmiş siparişler iptal edilemez. Lütfen iade işlemini kullanınız.");
        }

        orderService.cancel(order.getId(), null);

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(), "/cancel-order", null,
                "Siparişiniz başarıyla iptal edildi."));
    }
}
