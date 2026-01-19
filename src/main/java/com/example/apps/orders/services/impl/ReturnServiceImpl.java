package com.example.apps.orders.services.impl;

import com.example.apps.orders.dtos.*;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.orders.entities.ReturnRequest;
import com.example.apps.orders.entities.ReturnRequestItem;
import com.example.apps.orders.enums.OrderStatus;
import com.example.apps.orders.enums.ReturnRequestStatus;
import com.example.apps.orders.exceptions.OrderException;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.orders.repositories.ReturnRepository;
import com.example.apps.orders.services.IReturnService;
import com.example.apps.payments.dtos.OrderReturnRequestDTO;
import com.example.apps.payments.services.IPaymentService;
import com.example.apps.products.services.IProductService;
import com.example.apps.shipments.dtos.GeliverMainResponse;
import com.example.apps.shipments.dtos.GeliverRecipientAddressRequest;
import com.example.apps.shipments.dtos.GeliverReturnRequest;
import com.example.apps.shipments.dtos.GeliverShipmentDataResponse; // Added
import com.example.apps.shipments.dtos.GeliverTransactionMainResponse;
import com.example.apps.shipments.entities.GeliverShipmentEntity;
import com.example.apps.shipments.services.IShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReturnServiceImpl implements IReturnService {

    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final IPaymentService paymentService;
    private final IProductService productService;
    private final IShipmentService shipmentService;
    private final com.example.apps.shipments.configurations.GeliverConfiguration geliverConfiguration;

    @Override
    @Transactional
    public ReturnRequestResponseDTO createReturnRequest(Long userId, CreateReturnRequestDTO request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new OrderException("You are not authorized to return this order.");
        }

        return createReturnRequestInternal(order, userId, "USER:" + userId, request);
    }

    @Override
    @Transactional
    public ReturnRequestResponseDTO createGuestReturnRequest(String orderNumber, String initiatorEmail,
            CreateReturnRequestDTO request) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException("Order not found"));

        // Double check email matches for safety (though token should have handled it)
        if (!order.getCustomerEmail().equalsIgnoreCase(initiatorEmail)) {
            log.warn("Guest return email mismatch. Order: {}, Request: {}", order.getCustomerEmail(), initiatorEmail);
            // We can proceed if token is valid, or block. Let's block for extra safety.
            throw new OrderException("Email mismatch for this order.");
        }

        return createReturnRequestInternal(order, null, "GUEST:" + initiatorEmail, request);
    }

    private ReturnRequestResponseDTO createReturnRequestInternal(Order order, Long userId, String initiator,
            CreateReturnRequestDTO request) {
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new OrderException("Sadece teslim edilmiş siparişler iade edilebilir.");
        }

        // 30 Days Return Policy
        if (order.getCreatedAt().isBefore(java.time.LocalDateTime.now().minusDays(30))) {
            throw new OrderException("Sipariş tarihi üzerinden 30 gün geçtiği için iade kabul edilememektedir.");
        }

        // Check if an active (non-rejected) return request already exists for this
        // order
        boolean hasActiveReturn = returnRepository.existsByOrderIdAndStatusNot(order.getId(),
                ReturnRequestStatus.REJECTED);
        if (hasActiveReturn) {
            throw new OrderException("Bu sipariş için zaten bir iade talebi mevcut.");
        }

        // Prevent duplicate returns for same items (idempotency)
        List<ReturnRequestItem> returnItems = new ArrayList<>();
        BigDecimal totalRefundAmount = BigDecimal.ZERO;

        // To calculate prorated discount:
        BigDecimal subTotal = order.getTotalAmount();
        // Wait, totalAmount is final price. We need subtotal before discount?
        // Order entity might store discountAmount?
        // Assuming Order has explicit discount info or we calculate it.
        // Current Order entity: totalAmount. Does it have subtotal?
        // If not, we iterate items to sum prices.
        BigDecimal calculatedSubtotal = order.getOrderItems().stream()
                .map(i -> i.getPrice().multiply(new BigDecimal(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate Discount Ratio
        // Discount = CalculatedSubtotal - OrderTotalAmount
        BigDecimal totalDiscount = calculatedSubtotal.subtract(order.getTotalAmount());
        if (totalDiscount.compareTo(BigDecimal.ZERO) < 0)
            totalDiscount = BigDecimal.ZERO; // Should not happen

        int totalReturnQty = 0;

        for (ReturnItemRequestDTO itemDTO : request.getItems()) {
            OrderItem orderItem = order.getOrderItems().stream()
                    .filter(i -> i.getId().equals(itemDTO.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new OrderException("Order item not found: " + itemDTO.getOrderItemId()));

            // Check quantity
            if (itemDTO.getQuantity() > orderItem.getQuantity()) {
                throw new OrderException(
                        "Return quantity cannot exceed purchased quantity for item: "
                                + orderItem.getProductVariantName());
            }

            // TODO: Check if this item quantity was already returned in another request?
            // Assuming simplified logic for MVP or trusted user input + admin check.

            ReturnRequestItem returnItem = ReturnRequestItem.builder()
                    .orderItemId(itemDTO.getOrderItemId())
                    .quantity(itemDTO.getQuantity())
                    .build();
            returnItems.add(returnItem);

            // Prorated Price Calculation: ItemPrice * Qty - (Prorated Discount)
            BigDecimal lineTotal = orderItem.getPrice().multiply(new BigDecimal(itemDTO.getQuantity()));

            BigDecimal lineDiscountShare = BigDecimal.ZERO;
            if (calculatedSubtotal.compareTo(BigDecimal.ZERO) > 0) {
                lineDiscountShare = totalDiscount.multiply(lineTotal).divide(calculatedSubtotal, 2,
                        RoundingMode.HALF_DOWN);
            }

            totalRefundAmount = totalRefundAmount.add(lineTotal.subtract(lineDiscountShare));
            totalReturnQty += itemDTO.getQuantity();
        }

        ReturnRequest returnRequest = ReturnRequest.builder()
                .orderId(order.getId())
                .userId(userId)
                .initiator(initiator)
                .status(ReturnRequestStatus.PENDING)
                .returnReason(request.getReturnReason())
                .description(request.getDescription())
                .refundAmount(totalRefundAmount)
                .items(returnItems)
                .build();

        returnItems.forEach(i -> i.setReturnRequest(returnRequest));

        // SAVE FIRST to validate DB constraints before calling external APIs
        ReturnRequest saved = returnRepository.save(returnRequest);

        // GELIVER INTEGRATION (only after successful DB save)
        try {
            String originalShipmentId = order.getGeliverShipmentId();
            log.info("Order {} has geliverShipmentId: {}", order.getOrderNumber(), originalShipmentId);

            // Fallback to searching if ID not in Order entity
            if (originalShipmentId == null || originalShipmentId.isBlank()) {
                log.info("GeliverShipmentId not in Order entity, searching in shipment table...");
                com.example.apps.shipments.dtos.ShipmentDTO originalShipment = shipmentService
                        .getShipmentByOrderNumber(order.getOrderNumber());
                if (originalShipment != null) {
                    originalShipmentId = originalShipment.getGeliverShipmentId();
                    log.info("Found shipment in table. GeliverShipmentId: {}", originalShipmentId);
                } else {
                    log.warn("No shipment found in table for order: {}", order.getOrderNumber());
                }
            }

            log.info("Starting Geliver return integration for Order: {}, Original Shipment ID: {}",
                    order.getOrderNumber(), originalShipmentId);

            if (originalShipmentId != null && !originalShipmentId.isBlank()) {
                // If original shipment was via Geliver, create return there
                try {
                    GeliverRecipientAddressRequest senderAddress = GeliverRecipientAddressRequest.builder()
                            .name(order.getCustomerName())
                            .phone(order.getCustomerPhone() != null ? order.getCustomerPhone() : "+905000000000")
                            .email(order.getCustomerEmail())
                            .address1(order.getShippingAddress().getAddressLine())
                            .cityCode(order.getShippingAddress().getCityCode())
                            .districtName(order.getShippingAddress().getDistrictName())
                            .countryCode("TR") // Geliver requires ISO country code, not country name
                            .build();

                    // List of providers to try
                    java.util.List<String> providersToTry = new java.util.ArrayList<>();

                    // 1. Try Original Provider from Order
                    String originalProviderName = order.getShippingProvider();
                    if (originalProviderName != null) {
                        String mappedCode = switch (originalProviderName) {
                            case "Sürat Kargo" -> "SURAT_STANDART";
                            case "Yurtiçi Kargo" -> "YURTICI_STANDART";
                            case "PTT Kargo" -> "PTT_STANDART";
                            case "Aras Kargo" -> "ARAS_STANDART";
                            case "Geliver Kargo" -> "GELIVER_STANDART";
                            case "DHL Ecommerce" -> "DHL_STANDART";
                            case "hepsiJET" -> "HEPSIJET_STANDART";
                            case "Kolay Gelsin" -> "KOLAYGELSIN_STANDART";
                            case "Paket Taxi" -> "PAKETTAXI_STANDART";
                            default -> null; // Unknown friendly name
                        };
                        if (mappedCode != null) {
                            log.info("Prioritizing original provider for return: {} ({})", originalProviderName,
                                    mappedCode);
                            providersToTry.add(mappedCode);
                        }
                    }

                    // 2. Configured Default
                    String configuredProvider = geliverConfiguration.getReturnProviderCode();
                    if (configuredProvider != null && !configuredProvider.isBlank()
                            && !providersToTry.contains(configuredProvider)) {
                        providersToTry.add(configuredProvider);
                    }

                    // 3. Fallbacks (Best coverage providers)
                    if (!providersToTry.contains("PTT_STANDART"))
                        providersToTry.add("PTT_STANDART");
                    if (!providersToTry.contains("YURTICI_STANDART"))
                        providersToTry.add("YURTICI_STANDART");
                    if (!providersToTry.contains("ARAS_STANDART"))
                        providersToTry.add("ARAS_STANDART");
                    if (!providersToTry.contains("GELIVER_STANDART"))
                        providersToTry.add("GELIVER_STANDART");

                    GeliverTransactionMainResponse response = null;
                    Exception lastException = null;

                    for (String providerCode : providersToTry) {
                        try {
                            log.info("Attempting Geliver return with provider: {}", providerCode);

                            // Create a new request object/builder for each attempt provided the
                            // providerCode is immutable in the builder?
                            // builder pattern is safe to reuse? No, better rebuild to be safe
                            GeliverReturnRequest attemptRequest = GeliverReturnRequest.builder()
                                    .isReturn(true)
                                    .willAccept(true)
                                    .providerServiceCode(providerCode)
                                    .count(1)
                                    .senderAddress(senderAddress)
                                    .build();

                            response = shipmentService.createReturnShipment(originalShipmentId, attemptRequest);

                            if (response != null && response.getData() != null) {
                                log.info("Success with provider: {}", providerCode);
                                break; // Success!
                            }
                        } catch (Exception e) {
                            log.warn("Failed with provider {}: {}", providerCode, e.getMessage());
                            lastException = e;
                            // Continue to next provider
                        }
                    }

                    if (response == null) {
                        if (lastException != null)
                            throw lastException; // Throw the last relevant exception
                        throw new OrderException("Kargo iade servisi yanıt vermedi.");
                    }

                    if (response != null && response.getData() != null) {
                        log.info("Geliver return response received: {}", response);
                        com.example.apps.shipments.dtos.GeliverTransactionDataResponse data = response.getData();
                        log.info("Geliver transaction data: ID={}, Type={}", data.getId(), data.getTransactionType());
                        // When purchasing returns (willAccept=true), response data has a nested
                        // 'shipment'
                        com.example.apps.shipments.dtos.GeliverTransactionShipmentResponse shipmentDetail = data
                                .getShipment();

                        if (shipmentDetail == null) {
                            log.warn("Geliver transaction data did not contain shipment details. Data: {}", data);
                        }

                        if (shipmentDetail != null) {
                            String barcode = shipmentDetail.getBarcode();
                            String labelUrl = shipmentDetail.getLabelUrl();
                            String geliverId = shipmentDetail.getId();
                            String providerService = shipmentDetail.getProviderServiceCode();

                            // Map provider code to a friendly name using exact codes from Geliver
                            String friendlyProvider = switch (providerService) {
                                case "SURAT_STANDART" -> "Sürat Kargo";
                                case "YURTICI_STANDART" -> "Yurtiçi Kargo";
                                case "PTT_STANDART", "PTT_KAPIDA_ODEME" -> "PTT Kargo";
                                case "DHL_STANDART" -> "DHL Ecommerce";
                                case "HEPSIJET_STANDART" -> "hepsiJET";
                                case "KOLAYGELSIN_STANDART" -> "Kolay Gelsin";
                                case "PAKETTAXI_STANDART" -> "Paket Taxi";
                                case "ARAS_STANDART" -> "Aras Kargo";
                                case "GELIVER_STANDART" -> "Geliver Kargo";
                                default -> providerService != null ? providerService : "Kargo";
                            };

                            saved.setBarcode(barcode != null ? barcode : geliverId); // Set explicit barcode field
                            saved.setShippingCode(barcode != null ? barcode : geliverId);
                            saved.setShippingProvider(friendlyProvider);
                            saved.setLabelUrl(labelUrl);
                            saved.setGeliverReturnShipmentId(geliverId);

                            // Update the saved entity with Geliver info
                            saved = returnRepository.save(saved);

                            log.info(
                                    "Geliver return shipment (transaction) created. Barcode: {}, Label: {}, Provider: {}",
                                    barcode, labelUrl, friendlyProvider);
                        } else {
                            // Fallback if no shipment object but transaction created
                            saved.setGeliverReturnShipmentId(data.getId());
                            saved = returnRepository.save(saved);
                        }
                    } else {
                        throw new OrderException(
                                "Geliver iade gönderisi oluşturulamadı. Lütfen daha sonra tekrar deneyin.");
                    }
                } catch (OrderException e) {
                    throw e; // Re-throw OrderException
                } catch (Exception e) {
                    log.error("Failed to create Geliver return shipment for Order: {}", order.getOrderNumber(), e);
                    throw new OrderException("Kargo iade işlemi başarısız: " + e.getMessage());
                }
            } else {
                // No Geliver shipment ID - cannot create return
                log.error("No Geliver shipment ID found for order {}. Cannot create return.", order.getOrderNumber());
                throw new OrderException("Bu sipariş için kargo bilgisi bulunamadı. İade talebi oluşturulamıyor.");
            }
        } catch (OrderException e) {
            throw e; // Re-throw OrderException
        } catch (Exception e) {
            log.error("Geliver integration failed for Order: {}", order.getOrderNumber(), e);
            throw new OrderException("İade işlemi sırasında bir hata oluştu: " + e.getMessage());
        }

        return mapToDTO(saved);
    }

    @Override
    public ReturnRequestResponseDTO getReturnRequestById(Long id, Long userId) {
        ReturnRequest request = returnRepository.findById(id)
                .orElseThrow(() -> new OrderException("Return request not found"));

        if (userId != null && !request.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        return mapToDTO(request);
    }

    @Override
    public List<ReturnRequestResponseDTO> getUserReturnRequests(Long userId) {
        return returnRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<ReturnRequestResponseDTO> getUserReturnRequests(Long userId, int page,
            int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<ReturnRequest> returnsPage = returnRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return returnsPage.map(this::mapToDTO);
    }

    @Override
    public List<ReturnRequestResponseDTO> getAllReturnRequests() {
        return returnRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReturnRequestResponseDTO> getReturnRequestsByStatus(ReturnRequestStatus status) {
        return returnRepository.findByStatus(status).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReturnRequestResponseDTO markAsReceived(Long requestId) {
        ReturnRequest request = returnRepository.findById(requestId)
                .orElseThrow(() -> new OrderException("Request not found"));
        if (request.getStatus() == ReturnRequestStatus.PENDING) {
            request.setStatus(ReturnRequestStatus.RECEIVED);
            returnRepository.save(request);
        }
        return mapToDTO(request);
    }

    @Override
    @Transactional
    public ReturnRequestResponseDTO approveReturnRequest(Long requestId, boolean restockItems) {
        ReturnRequest request = returnRepository.findById(requestId)
                .orElseThrow(() -> new OrderException("Return Request not found"));

        if (request.getStatus() == ReturnRequestStatus.APPROVED
                || request.getStatus() == ReturnRequestStatus.COMPLETED) {
            throw new OrderException("Request already approved.");
        }

        // Trigger Refund
        OrderReturnRequestDTO paymentRefundRequest = new OrderReturnRequestDTO();
        paymentRefundRequest.setOrderId(request.getOrderId());
        paymentRefundRequest.setReturnReason(request.getReturnReason().getLabel());

        List<Long> itemIds = request.getItems().stream().map(ReturnRequestItem::getOrderItemId)
                .collect(Collectors.toList());
        paymentRefundRequest.setOrderItemIds(itemIds);
        paymentRefundRequest.setRestoreStock(restockItems);
        paymentRefundRequest.setRefundAmount(request.getRefundAmount()); // Pass calculated (discounted) amount

        paymentService.refundPartialPayment(paymentRefundRequest);

        // PaymentService.refundPartialPayment() automatically does:
        // productService.increaseStock(...)

        // ISSUE: If I want "restockItems" control, `PaymentService` logic forces stock
        // increase.
        // I might need to update `PaymentService` to NOT increase stock if I want to
        // control it here.
        // OR rely on PaymentService for now (user requirement "B. Stok Yönetimi
        // Stratejisi" implies conditional).

        // To fix this without breaking PaymentService interface too much:
        // Since `PaymentService` is "legacy" for immediate returns, and `ReturnService`
        // is new:
        // Proper way: Refactor `PaymentService.refundPartialPayment` to accept a
        // `boolean restoreStock` flag?
        // Or handle stock restoration separately and remove it from `PaymentService`?

        // For now, assuming I can't easily change PaymentService safely in this step
        // without reading it again carefully.
        // User asked for "Stock Management Strategy".

        // Let's assume for this MVP step: PaymentService ALWAYS restores stock.
        // If Restock=FALSE (Damaged), we need to manually DECREASE stock back? That's
        // hacky.

        // Better: I will edit PaymentService next to allow suppressing stock update.

        request.setStatus(ReturnRequestStatus.APPROVED);
        request.setRestockItems(restockItems);
        returnRepository.save(request);

        return mapToDTO(request);
    }

    @Override
    @Transactional
    public ReturnRequestResponseDTO rejectReturnRequest(Long requestId, String reason) {
        ReturnRequest request = returnRepository.findById(requestId)
                .orElseThrow(() -> new OrderException("Request not found"));
        request.setStatus(ReturnRequestStatus.REJECTED);
        request.setAdminNote(reason);
        returnRepository.save(request);
        return mapToDTO(request);
    }

    @Override
    @Transactional
    public ReturnRequestResponseDTO cancelReturnRequest(Long userId, Long requestId) {
        ReturnRequest request = returnRepository.findById(requestId)
                .orElseThrow(() -> new OrderException("Request not found"));

        if (!request.getUserId().equals(userId)) {
            throw new OrderException("You are not authorized to cancel this return request.");
        }

        if (request.getStatus() != ReturnRequestStatus.PENDING) {
            throw new OrderException("Only PENDING return requests can be cancelled.");
        }

        // Cancel Geliver shipment if one was created
        if (request.getGeliverReturnShipmentId() != null && !request.getGeliverReturnShipmentId().isBlank()) {
            try {
                log.info("Cancelling associated Geliver return shipment: {}", request.getGeliverReturnShipmentId());
                shipmentService.cancelShipmentByID(request.getGeliverReturnShipmentId());
            } catch (Exception e) {
                // Log but continue, or throw?
                // Since this is user-facing cancel, maybe we should warn but allow internal
                // cancel?
                // Or if it fails, maybe user shouldn't be able to cancel?
                // Let's log error but proceed to cancel internal request so user isn't stuck.
                log.error("Failed to cancel Geliver return shipment: {}", e.getMessage());
                // Optional: throw new OrderException("Kargo iptal edilemedi, lütfen desteğe
                // başvurun.");
            }
        }

        request.setStatus(ReturnRequestStatus.CANCELLED);
        returnRepository.save(request);
        return mapToDTO(request);
    }

    @Override
    public ReturnPageResult getAllReturns(int page, int size, String sortField, String sortOrder, Long userId,
            ReturnRequestStatus status) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                sortOrder.equalsIgnoreCase("ASC") ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC,
                sortField);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                sort);

        Specification<ReturnRequest> spec = (root, query, cb) -> cb.conjunction();

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        org.springframework.data.domain.Page<ReturnRequest> returnsPage = returnRepository.findAll(spec, pageable);
        List<ReturnRequestResponseDTO> dtos = returnsPage.getContent().stream().map(this::mapToDTO).toList();

        return new ReturnPageResult(dtos, returnsPage.getTotalElements());
    }

    private ReturnRequestResponseDTO mapToDTO(ReturnRequest entity) {
        // Fetch order to get orderNumber
        String orderNumber = null;
        java.util.Map<Long, com.example.apps.orders.entities.OrderItem> orderItemMap = new java.util.HashMap<>();

        orderRepository.findById(entity.getOrderId()).ifPresent(order -> {
            // Set orderNumber - using a holder pattern since we can't directly assign in
            // lambda
            orderItemMap.put(-1L, null); // placeholder to access order
            for (com.example.apps.orders.entities.OrderItem item : order.getOrderItems()) {
                orderItemMap.put(item.getId(), item);
            }
        });

        // Get orderNumber separately
        String fetchedOrderNumber = orderRepository.findById(entity.getOrderId())
                .map(o -> o.getOrderNumber())
                .orElse(null);

        return ReturnRequestResponseDTO.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .orderNumber(fetchedOrderNumber)
                .userId(entity.getUserId())
                .status(entity.getStatus())
                .returnReason(entity.getReturnReason())
                .description(entity.getDescription())
                .adminNote(entity.getAdminNote())
                .refundAmount(entity.getRefundAmount())
                .barcode(entity.getBarcode())
                .shippingCode(entity.getShippingCode())
                .shippingProvider(entity.getShippingProvider())
                .trackingUrl(entity.getTrackingUrl())
                .labelUrl(entity.getLabelUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .items(entity.getItems().stream().map(i -> {
                    com.example.apps.orders.entities.OrderItem orderItem = orderItemMap.get(i.getOrderItemId());
                    return ReturnItemResponseDTO.builder()
                            .orderItemId(i.getOrderItemId())
                            .quantity(i.getQuantity())
                            .productName(orderItem != null ? orderItem.getProductVariantName() : null)
                            .variantName(orderItem != null && orderItem.getSize() != null ? orderItem.getSize().name()
                                    : null)
                            .size(orderItem != null && orderItem.getSize() != null ? orderItem.getSize().name() : null)
                            .price(orderItem != null ? orderItem.getPrice() : null)
                            .build();
                }).collect(Collectors.toList()))
                .build();
    }
}
