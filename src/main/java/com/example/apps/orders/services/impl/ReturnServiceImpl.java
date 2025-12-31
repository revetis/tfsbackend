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

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new OrderException("Only delivered orders can be returned.");
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
                .status(ReturnRequestStatus.PENDING)
                .returnReason(request.getReturnReason())
                .description(request.getDescription())
                .refundAmount(totalRefundAmount)
                .items(returnItems)
                .build();

        returnItems.forEach(i -> i.setReturnRequest(returnRequest));

        // GELIVER INTEGRATION
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

                    GeliverReturnRequest geliverRequest = GeliverReturnRequest.builder()
                            .isReturn(true)
                            .willAccept(true)
                            .providerServiceCode(geliverConfiguration.getReturnProviderCode())
                            .count(1)
                            .senderAddress(senderAddress)
                            .build();

                    log.info("Calling Geliver createReturnShipment for original ID: {}", originalShipmentId);
                    GeliverTransactionMainResponse response = shipmentService
                            .createReturnShipment(originalShipmentId, geliverRequest);

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

                            returnRequest.setShippingCode(barcode != null ? barcode : geliverId);
                            returnRequest.setShippingProvider(friendlyProvider);
                            returnRequest.setLabelUrl(labelUrl);
                            returnRequest.setGeliverReturnShipmentId(geliverId);

                            log.info(
                                    "Geliver return shipment (transaction) created. Barcode: {}, Label: {}, Provider: {}",
                                    barcode, labelUrl, friendlyProvider);
                        } else {
                            // Fallback if no shipment object but transaction created
                            returnRequest.setGeliverReturnShipmentId(data.getId());
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

        ReturnRequest saved = returnRepository.save(returnRequest);
        return mapToDTO(saved);
    }

    @Override
    public ReturnRequestResponseDTO getReturnRequestById(Long id) {
        ReturnRequest request = returnRepository.findById(id)
                .orElseThrow(() -> new OrderException("Return request not found"));
        return mapToDTO(request);
    }

    @Override
    public List<ReturnRequestResponseDTO> getUserReturnRequests(Long userId) {
        return returnRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
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
                .shippingCode(entity.getShippingCode())
                .shippingProvider(entity.getShippingProvider())
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
                            .build();
                }).collect(Collectors.toList()))
                .build();
    }
}
