package com.example.apps.shipments.services.impl;

import com.example.apps.shipments.configurations.GeliverConfiguration;
import com.example.apps.shipments.dtos.GeliverCityListResponse;
import com.example.apps.shipments.dtos.GeliverDistrictListResponse;
import com.example.apps.shipments.dtos.GeliverMainResponse;
import com.example.apps.shipments.dtos.GeliverReturnRequest;
import com.example.apps.shipments.dtos.GeliverShipmentCreateRequest;
import com.example.apps.shipments.dtos.GeliverTransactionCreateRequest;
import com.example.apps.shipments.dtos.GeliverTransactionMainResponse;
import com.example.apps.shipments.entities.GeliverShipmentEntity;
import com.example.apps.shipments.entities.ShipmentEventEntity;
import com.example.apps.shipments.enums.ShipmentStatus;
import com.example.apps.shipments.exceptions.ShipmentException;
import com.example.apps.shipments.repositories.GeliverShipmentEntityRepository;
import com.example.apps.shipments.repositories.ShipmentEventRepository;
import com.example.apps.shipments.services.IShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.resource.NotSupportedException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.apps.shipments.dtos.ShipmentDTO;
import com.example.apps.shipments.dtos.ShipmentEventDTO;
import com.example.apps.shipments.dtos.GeliverWebhookRequest;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.enums.OrderStatus;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;
import com.example.apps.payments.services.IPaymentService;
import org.springframework.context.annotation.Lazy;

@Service
@Slf4j
public class GeliverServiceImpl implements IShipmentService {

    private final WebClient geliverWebClient;
    private final GeliverConfiguration geliverConfiguration;
    private final GeliverShipmentEntityRepository geliverShipmentEntityRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final OrderRepository orderRepository;
    private final IN8NService n8nService;
    private final N8NProperties n8NProperties;
    private final IPaymentService paymentService;

    public GeliverServiceImpl(WebClient geliverWebClient, GeliverConfiguration geliverConfiguration,
            GeliverShipmentEntityRepository geliverShipmentEntityRepository,
            ShipmentEventRepository shipmentEventRepository,
            OrderRepository orderRepository,
            IN8NService n8nService,
            N8NProperties n8NProperties,
            @Lazy IPaymentService paymentService) {
        this.geliverWebClient = geliverWebClient;
        this.geliverConfiguration = geliverConfiguration;
        this.geliverShipmentEntityRepository = geliverShipmentEntityRepository;
        this.shipmentEventRepository = shipmentEventRepository;
        this.orderRepository = orderRepository;
        this.n8nService = n8nService;
        this.n8NProperties = n8NProperties;
        this.paymentService = paymentService;
    }

    @Override
    public GeliverCityListResponse getCities(String countryCode) {
        log.info("Fetching cities for country: {}", countryCode);
        return geliverWebClient.get()
                .uri(uri -> uri.path("/cities").queryParam("countryCode", countryCode).build())
                .retrieve()
                .bodyToMono(GeliverCityListResponse.class)
                .block();
    }

    @Override
    public GeliverDistrictListResponse getDistricts(String countryCode, String cityCode) {
        if (cityCode == null || cityCode.isBlank()) {
            throw new ShipmentException("City code cannot be null or empty.");
        }

        log.info("Fetching districts for city code: {} in country: {}", cityCode, countryCode);
        return geliverWebClient.get()
                .uri(uri -> uri.path("/districts")
                        .queryParam("countryCode", countryCode)
                        .queryParam("cityCode", cityCode).build())
                .retrieve()
                .bodyToMono(GeliverDistrictListResponse.class)
                .block();
    }

    @Override
    public GeliverMainResponse createShipment(GeliverShipmentCreateRequest incomingRequest) {
        log.info("Creating shipment request for order: {}", incomingRequest.getOrder().getOrderNumber());

        // Sanitize phone number if present
        if (incomingRequest.getRecipientAddress() != null && incomingRequest.getRecipientAddress().getPhone() != null) {
            incomingRequest.getRecipientAddress()
                    .setPhone(cleanPhoneNumber(incomingRequest.getRecipientAddress().getPhone()));
        }

        // Enriching request with configuration parameters
        incomingRequest.setTest(Boolean.parseBoolean(geliverConfiguration.getTestMode()));
        incomingRequest.setSenderAddressID(geliverConfiguration.getSenderAddressId());
        incomingRequest.setReturnAddressID(geliverConfiguration.getReturnAddressId());
        incomingRequest.setDistanceUnit("cm");
        incomingRequest.setMassUnit("kg");

        return geliverWebClient.post()
                .uri("/shipments")
                .bodyValue(incomingRequest)
                .retrieve()
                .onStatus(status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new ShipmentException("Geliver API Error: " + error))))
                .bodyToMono(GeliverMainResponse.class)
                .doOnNext(res -> log.info("Shipment created successfully. Shipment ID: {}",
                        res.getData() != null ? res.getData().getId() : "N/A"))
                .block();
    }

    @Override
    public GeliverTransactionMainResponse createReturnShipment(String originalShipmentId,
            GeliverReturnRequest request) {
        log.info("Creating return shipment for Original Shipment ID: {}", originalShipmentId);

        // Sanitize phone number if present
        if (request.getSenderAddress() != null && request.getSenderAddress().getPhone() != null) {
            request.getSenderAddress().setPhone(cleanPhoneNumber(request.getSenderAddress().getPhone()));
        }

        // Log the full request for debugging
        log.info("Geliver Return Request: isReturn={}, willAccept={}, providerServiceCode={}, count={}",
                request.getIsReturn(), request.getWillAccept(), request.getProviderServiceCode(), request.getCount());
        log.info(
                "Geliver Return Sender Address: name={}, phone={}, address1={}, countryCode={}, cityCode={}, districtName={}",
                request.getSenderAddress().getName(),
                request.getSenderAddress().getPhone(),
                request.getSenderAddress().getAddress1(),
                request.getSenderAddress().getCountryCode(),
                request.getSenderAddress().getCityCode(),
                request.getSenderAddress().getDistrictName());

        return geliverWebClient.post()
                .uri("/shipments/{id}", originalShipmentId)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(error -> Mono
                                        .error(new ShipmentException("Geliver API Error (Return): " + error))))
                .bodyToMono(GeliverTransactionMainResponse.class)
                .doOnNext(res -> log.info("Return Shipment transaction created successfully. ID: {}",
                        res.getData() != null ? res.getData().getId() : "N/A"))
                .block();
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public GeliverTransactionMainResponse offerPurchase(GeliverTransactionCreateRequest transactionRequest) {
        log.info("Purchasing offer for provider service: {}", transactionRequest.getProviderServiceCode());

        // Sanitize phone number if present in shipment data
        if (transactionRequest.getShipment() != null &&
                transactionRequest.getShipment().getRecipientAddress() != null &&
                transactionRequest.getShipment().getRecipientAddress().getPhone() != null) {

            transactionRequest.getShipment().getRecipientAddress().setPhone(
                    cleanPhoneNumber(transactionRequest.getShipment().getRecipientAddress().getPhone()));
        }

        transactionRequest.getShipment().setTest(Boolean.parseBoolean(geliverConfiguration.getTestMode()));

        GeliverTransactionMainResponse responseGeliver = geliverWebClient.post()
                .uri("/transactions")
                .bodyValue(transactionRequest)
                .retrieve()
                .onStatus(status -> status.isError(),
                        response -> response.bodyToMono(String.class).flatMap(
                                error -> Mono.error(new ShipmentException("Purchase Transaction Failed: " + error))))
                .bodyToMono(GeliverTransactionMainResponse.class)
                .doOnNext(res -> log.info("Transaction completed. Barcode: {}",
                        res.getData().getShipment().getBarcode()))
                .block();

        GeliverShipmentEntity shipment = new GeliverShipmentEntity();
        shipment.setOrderNumber(transactionRequest.getShipment().getOrder().getOrderNumber());
        shipment.setGeliverShipmentId(responseGeliver.getData().getShipment().getId());
        shipment.setTrackingNumber(responseGeliver.getData().getShipment().getBarcode());
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setLabelUrl(responseGeliver.getData().getShipment().getLabelUrl());
        shipment.setTrackingUrl(responseGeliver.getData().getShipment().getTrackingUrl());

        // Parse total amount safely as it might be a decimal string
        String totalAmountStr = responseGeliver.getData().getTotalAmount();
        if (totalAmountStr != null) {
            shipment.setTotalAmount(new BigDecimal(totalAmountStr));
        }

        geliverShipmentEntityRepository.save(shipment);
        log.info("Shipment record saved for order: {} with barcode: {}", shipment.getOrderNumber(),
                shipment.getTrackingNumber());

        return responseGeliver;
    }

    @Override
    public GeliverMainResponse listShipment(String orderNumber) {
        log.info("Listing shipments for order number: {}", orderNumber);
        try {
            return geliverWebClient.get()
                    .uri(uri -> uri.path("/shipments").queryParam("orderNumber", orderNumber).build())
                    .retrieve()
                    .bodyToMono(GeliverMainResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error occurred while listing shipment for order {}: {}", orderNumber, e.getMessage());
            throw new ShipmentException("Could not list shipments for order: " + orderNumber);
        }
    }

    @Override
    public GeliverMainResponse cancelShipmentByID(String shipmentID) {
        log.info("Cancelling shipment with Geliver ID: {}", shipmentID);
        try {
            return geliverWebClient.delete()
                    .uri("/shipments/{id}", shipmentID)
                    .retrieve()
                    .onStatus(status -> status.isError(),
                            response -> response.bodyToMono(String.class).flatMap(
                                    error -> Mono.error(new ShipmentException("Cancel Shipment Failed: " + error))))
                    .bodyToMono(GeliverMainResponse.class)
                    .doOnNext(res -> log.info("Shipment {} cancelled successfully", shipmentID))
                    .block();
        } catch (Exception e) {
            log.error("Error occurred while cancelling shipment {}: {}", shipmentID, e.getMessage());
            throw new ShipmentException("Could not cancel shipment: " + shipmentID);
        }
    }

    @Override
    public GeliverMainResponse cancelShipment(String orderNumber) throws NotSupportedException {
        log.info("Cancelling shipment for order number via lookup: {}", orderNumber);
        GeliverShipmentEntity shipment = geliverShipmentEntityRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ShipmentException("Shipment for order not found: " + orderNumber));

        GeliverMainResponse response = cancelShipmentByID(shipment.getGeliverShipmentId());

        if (response != null && response.isResult()) {
            shipment.setStatus(ShipmentStatus.CANCELLED);
            geliverShipmentEntityRepository.save(shipment);
            log.info("Shipment status updated to CANCELLED for order: {}", orderNumber);
        } else {
            log.warn("Geliver API returned false result for shipment cancellation. Order: {}", orderNumber);
        }

        return response;
    }

    @Override
    public List<ShipmentDTO> getAllShipments() {
        return geliverShipmentEntityRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ShipmentPageResult getAllShipments(int page, int size, String sortField, String sortOrder, String search,
            String status, Long userId) {
        // Build sort
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                sortOrder.equalsIgnoreCase("ASC") ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC,
                sortField);

        // Get all shipments and filter in memory (matching existing pattern in this
        // service)
        List<GeliverShipmentEntity> allShipments = geliverShipmentEntityRepository.findAll(sort);

        // Apply filters
        java.util.stream.Stream<GeliverShipmentEntity> stream = allShipments.stream();

        if (userId != null) {
            List<String> userOrderNumbers = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(Order::getOrderNumber)
                    .collect(Collectors.toList());
            stream = stream.filter(s -> userOrderNumbers.contains(s.getOrderNumber()));
        }

        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            stream = stream.filter(s -> (s.getOrderNumber() != null
                    && s.getOrderNumber().toLowerCase().contains(searchLower)) ||
                    (s.getTrackingNumber() != null && s.getTrackingNumber().toLowerCase().contains(searchLower)) ||
                    (s.getGeliverShipmentId() != null && s.getGeliverShipmentId().toLowerCase().contains(searchLower)));
        }

        if (status != null && !status.isBlank()) {
            try {
                ShipmentStatus statusEnum = ShipmentStatus.valueOf(status);
                stream = stream.filter(s -> s.getStatus() == statusEnum);
            } catch (IllegalArgumentException ignored) {
            }
        }

        List<GeliverShipmentEntity> filteredShipments = stream.collect(Collectors.toList());
        long totalCount = filteredShipments.size();

        // Apply pagination
        int fromIndex = Math.min(page * size, filteredShipments.size());
        int toIndex = Math.min((page + 1) * size, filteredShipments.size());
        List<GeliverShipmentEntity> pagedShipments = filteredShipments.subList(fromIndex, toIndex);

        // Map to DTOs
        List<ShipmentDTO> dtos = pagedShipments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new ShipmentPageResult(dtos, totalCount);
    }

    @Override
    public ShipmentDTO getShipmentById(Long id) {
        return geliverShipmentEntityRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ShipmentException("Shipment not found"));
    }

    @Override
    public void deleteShipmentById(Long id) {
        geliverShipmentEntityRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void processTrackingWebhook(GeliverWebhookRequest request) {
        if (request.getData() == null) {
            log.warn("Geliver webhook received with no data.");
            return;
        }

        String geliverStatus = null;
        String subStatusCode = null;

        if (request.getData().getTrackingStatus() != null) {
            geliverStatus = request.getData().getTrackingStatus().getTrackingStatusCode();
            subStatusCode = request.getData().getTrackingStatus().getTrackingSubStatusCode();
        }

        // Fallback to statusCode if trackingStatusCode is not available
        if (geliverStatus == null || geliverStatus.isBlank()) {
            geliverStatus = request.getData().getStatusCode();
        }

        String orderNumber = request.getData().getOrderNumber();
        String shipmentId = request.getData().getId();

        log.info(
                "Processing Geliver tracking webhook for order: {}, shipmentId: {}, trackingStatusCode: {}, subStatus: {}, rawStatusCode: {}",
                orderNumber, shipmentId, geliverStatus, subStatusCode, request.getData().getStatusCode());

        if ((shipmentId == null || shipmentId.isBlank()) && (orderNumber == null || orderNumber.isBlank())) {
            log.info("Geliver webhook received with no shipment identification. Likely a test request.");
            return;
        }

        Optional<GeliverShipmentEntity> shipmentOpt = Optional.empty();
        if (shipmentId != null && !shipmentId.isBlank()) {
            shipmentOpt = geliverShipmentEntityRepository.findByGeliverShipmentId(shipmentId);
        }

        if (shipmentOpt.isEmpty() && orderNumber != null && !orderNumber.isBlank()) {
            shipmentOpt = geliverShipmentEntityRepository.findByOrderNumber(orderNumber);
        }

        if (shipmentOpt.isEmpty()) {
            log.warn("Shipment not found for Geliver ID: {} or Order Number: {}", shipmentId, orderNumber);
            return;
        }

        GeliverShipmentEntity shipment = shipmentOpt.get();
        ShipmentStatus previousStatus = shipment.getStatus(); // Store previous status
        ShipmentStatus internalStatus = mapToInternalStatus(geliverStatus);

        // Check if status actually changed
        boolean statusChanged = previousStatus != internalStatus;

        shipment.setStatus(internalStatus);
        if (request.getData().getTrackingNumber() != null) {
            shipment.setTrackingNumber(request.getData().getTrackingNumber());
        }
        if (request.getData().getTrackingUrl() != null) {
            shipment.setTrackingUrl(request.getData().getTrackingUrl());
        }
        geliverShipmentEntityRepository.save(shipment);

        // Save shipment event (always save for tracking history)
        saveShipmentEvent(shipment, request.getData());

        // Update Order status and send email ONLY if status actually changed
        orderRepository.findByOrderNumber(shipment.getOrderNumber()).ifPresent(order -> {
            if (statusChanged) {
                log.info("Shipment status changed from {} to {} for order: {}", previousStatus, internalStatus,
                        order.getOrderNumber());
                updateOrderStatus(order, internalStatus);
                orderRepository.save(order);
                triggerN8NEmail(order, shipment, internalStatus, request.getData());

                // Gönderi iptal, iade veya teslimat başarısız olduysa otomatik para iadesi yap
                if (internalStatus == ShipmentStatus.CANCELLED || internalStatus == ShipmentStatus.RETURNED
                        || internalStatus == ShipmentStatus.FAILED) {
                    handleShipmentCancellation(order);
                }
            } else {
                log.info("Shipment status unchanged ({}) for order: {}, skipping email", internalStatus,
                        order.getOrderNumber());
            }
        });
    }

    /**
     * Geliver gönderi iptal edildiğinde siparişi iptal edip para iadesi başlatır
     */
    private void handleShipmentCancellation(Order order) {
        try {
            // Ödeme zaten iade edilmişse tekrar işlem yapma
            if (order.getPaymentStatus() == com.example.apps.payments.enums.PaymentStatus.REFUNDED) {
                log.info("Order {} payment is already refunded, skipping duplicate refund", order.getOrderNumber());
                return;
            }

            log.info("Shipment cancelled by Geliver for order: {}. Initiating automatic refund.",
                    order.getOrderNumber());

            // Sipariş durumunu CANCELLED yap
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            // Iyzico üzerinden para iadesi başlat
            paymentService.returnPayment(order.getOrderNumber());

            log.info("Automatic refund initiated successfully for cancelled shipment. Order: {}",
                    order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to process automatic refund for cancelled shipment. Order: {}, Error: {}",
                    order.getOrderNumber(), e.getMessage(), e);
            // Hata olsa bile log'a yazıp devam et, manuel müdahale gerekebilir
        }
    }

    private void saveShipmentEvent(GeliverShipmentEntity shipment,
            com.example.apps.shipments.dtos.GeliverWebhookData data) {
        try {
            ShipmentEventEntity event = new ShipmentEventEntity();
            event.setShipmentId(shipment.getId());
            event.setOrderNumber(shipment.getOrderNumber());
            event.setGeliverShipmentId(shipment.getGeliverShipmentId());
            event.setTrackingNumber(data.getTrackingNumber());
            event.setStatusCode(data.getStatusCode());
            event.setCarrier(data.getCarrier());
            event.setTrackingUrl(data.getTrackingUrl());
            event.setDeliveryDate(data.getDeliveryDate());
            event.setFailureReason(data.getFailureReason());
            event.setReturnDetails(data.getReturnDetails());

            if (data.getTrackingStatus() != null) {
                event.setTrackingStatusCode(data.getTrackingStatus().getTrackingStatusCode());
                event.setTrackingSubStatusCode(data.getTrackingStatus().getTrackingSubStatusCode());
                event.setStatusDetails(data.getTrackingStatus().getStatusDetails());
                event.setStatusDate(data.getTrackingStatus().getStatusDate());
                event.setLocationName(data.getTrackingStatus().getLocationName());
                event.setLocationLat(data.getTrackingStatus().getLocationLat());
                event.setLocationLng(data.getTrackingStatus().getLocationLng());
            }

            shipmentEventRepository.save(event);
            log.info("Shipment event saved for order: {}, status: {}", shipment.getOrderNumber(), data.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to save shipment event for order: {}", shipment.getOrderNumber(), e);
        }
    }

    private ShipmentStatus mapToInternalStatus(String geliverStatus) {
        if (geliverStatus == null)
            return ShipmentStatus.PENDING;

        return switch (geliverStatus.toUpperCase()) {
            // PRE_TRANSIT: Kargo henüz yola çıkmadı
            case "PRE_TRANSIT", "TRACKING_CODE_CREATED", "CREATED" -> ShipmentStatus.CREATED;
            // TRANSIT: Kargo yolda (tüm ara durumlar dahil - aktarma, dağıtımda vs.)
            case "TRANSIT", "SHIPPED", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY" -> ShipmentStatus.SHIPPED;
            // DELIVERED: Teslim edildi
            case "DELIVERED" -> ShipmentStatus.DELIVERED;
            // RETURNED: İade edildi
            case "RETURNED" -> ShipmentStatus.RETURNED;
            // CANCELED: İptal edildi
            case "CANCELED", "CANCELLED" -> ShipmentStatus.CANCELLED;
            // FAILURE: Teslimat başarısız (kayıp, dağıtılamıyor vs.)
            case "FAILURE" -> ShipmentStatus.FAILED;
            // UNKNOWN: Bilinmeyen durum
            case "UNKNOWN" -> ShipmentStatus.PENDING;
            default -> {
                log.warn("Unknown Geliver status code: '{}', defaulting to PENDING", geliverStatus);
                yield ShipmentStatus.PENDING;
            }
        };
    }

    private void updateOrderStatus(Order order, ShipmentStatus shipmentStatus) {
        switch (shipmentStatus) {
            case SHIPPED -> order.setStatus(OrderStatus.SHIPPED);
            case DELIVERED -> order.setStatus(OrderStatus.DELIVERED);
            case RETURNED -> order.setStatus(OrderStatus.RETURNED);
            case CANCELLED, FAILED -> order.setStatus(OrderStatus.CANCELLED);
            default -> {
                /* Keep current status */ }
        }
    }

    private void triggerN8NEmail(Order order, GeliverShipmentEntity shipment, ShipmentStatus status,
            com.example.apps.shipments.dtos.GeliverWebhookData data) {
        String webhookUrl = null;
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", order.getCustomerEmail());
        payload.put("name", order.getCustomerName());
        payload.put("orderNumber", order.getOrderNumber());
        payload.put("trackingNumber", shipment.getTrackingNumber());
        // Use carrier from webhook, fallback to order's shipping provider
        String carrier = data.getCarrier();
        if (carrier == null || carrier.isBlank()) {
            carrier = order.getShippingProvider();
        }
        payload.put("carrier", carrier);
        // Use tracking URL from order first (has correct carrier-specific URL),
        // then fallback to shipment's stored URL, then webhook data
        String trackingUrl = order.getTrackingUrl();
        if (trackingUrl == null || trackingUrl.isBlank() || trackingUrl.contains("example.com")) {
            trackingUrl = shipment.getTrackingUrl();
        }
        if (trackingUrl == null || trackingUrl.isBlank() || trackingUrl.contains("example.com")) {
            trackingUrl = data.getTrackingUrl();
        }
        payload.put("trackingUrl", trackingUrl);
        payload.put("labelUrl", shipment.getLabelUrl());

        switch (status) {
            case CREATED -> {
                // No email for CREATED - just tracking code generated, not shipped yet
                log.info("Shipment created for order: {}, no email sent (waiting for SHIPPED)", order.getOrderNumber());
            }
            case SHIPPED -> webhookUrl = n8NProperties.getWebhook().getShipmentShipped();
            case DELIVERED -> {
                webhookUrl = n8NProperties.getWebhook().getShipmentDelivered();
                String deliveryDate = data.getDeliveryDate();
                if (deliveryDate == null || deliveryDate.isBlank()) {
                    deliveryDate = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm",
                                    java.util.Locale.of("tr", "TR")));
                }
                payload.put("deliveryDate", deliveryDate);
            }
            case RETURNED -> {
                webhookUrl = n8NProperties.getWebhook().getShipmentReturned();
                payload.put("returnDetails", data.getReturnDetails());
            }
            default -> {
                // FAILURE durumu için e-posta gönder
                String rawStatus = data.getStatusCode();
                if (rawStatus != null && (rawStatus.equalsIgnoreCase("FAILURE") || rawStatus.contains("FAILED"))) {
                    webhookUrl = n8NProperties.getWebhook().getShipmentFailed();
                    payload.put("failureReason", data.getFailureReason());
                    // Alt durum kodunu da ekle (package_lost, package_undeliverable vs.)
                    if (data.getTrackingStatus() != null
                            && data.getTrackingStatus().getTrackingSubStatusCode() != null) {
                        payload.put("failureType", data.getTrackingStatus().getTrackingSubStatusCode());
                    }
                    log.info("Shipment failure detected for order: {}, reason: {}", order.getOrderNumber(),
                            data.getFailureReason());
                }
            }
        }

        if (webhookUrl != null) {
            log.info("Triggering n8n email for status: {} to {}", status, webhookUrl);
            n8nService.triggerWorkflow(webhookUrl, payload);
        }
    }

    private ShipmentDTO toDTO(GeliverShipmentEntity entity) {
        ShipmentDTO dto = new ShipmentDTO();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().name());
        }
        return dto;
    }

    @Override
    public List<ShipmentEventDTO> getShipmentEvents(Long shipmentId) {
        return shipmentEventRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId)
                .stream()
                .map(this::eventToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentEventDTO> getShipmentEventsByOrderNumber(String orderNumber) {
        return shipmentEventRepository.findByOrderNumberOrderByCreatedAtDesc(orderNumber)
                .stream()
                .map(this::eventToDTO)
                .collect(Collectors.toList());
    }

    private ShipmentEventDTO eventToDTO(ShipmentEventEntity entity) {
        ShipmentEventDTO dto = new ShipmentEventDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public ShipmentDTO getShipmentByOrderNumber(String orderNumber) {
        return geliverShipmentEntityRepository.findByOrderNumber(orderNumber)
                .map(this::toDTO)
                .orElse(null);
    }

    private String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null)
            return null;
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("90")) {
            return "+" + cleaned;
        }
        if (cleaned.startsWith("0")) {
            return "+90" + cleaned.substring(1);
        }
        return "+90" + cleaned;
    }
}