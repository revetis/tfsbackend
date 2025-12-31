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

    public GeliverServiceImpl(WebClient geliverWebClient, GeliverConfiguration geliverConfiguration,
            GeliverShipmentEntityRepository geliverShipmentEntityRepository,
            ShipmentEventRepository shipmentEventRepository,
            OrderRepository orderRepository,
            IN8NService n8nService,
            N8NProperties n8NProperties) {
        this.geliverWebClient = geliverWebClient;
        this.geliverConfiguration = geliverConfiguration;
        this.geliverShipmentEntityRepository = geliverShipmentEntityRepository;
        this.shipmentEventRepository = shipmentEventRepository;
        this.orderRepository = orderRepository;
        this.n8nService = n8nService;
        this.n8NProperties = n8NProperties;
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
    public ShipmentDTO getShipmentById(Long id) {
        return geliverShipmentEntityRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ShipmentException("Shipment not found"));
    }

    @Override
    @Transactional
    public void processTrackingWebhook(GeliverWebhookRequest request) {
        if (request.getData() == null) {
            log.warn("Geliver webhook received with no data.");
            return;
        }

        String geliverStatus = request.getData().getStatusCode();
        String orderNumber = request.getData().getOrderNumber();
        String shipmentId = request.getData().getId();

        log.info("Processing Geliver tracking webhook for order: {}, shipmentId: {}, status: {}", orderNumber,
                shipmentId, geliverStatus);

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
        ShipmentStatus internalStatus = mapToInternalStatus(geliverStatus);

        shipment.setStatus(internalStatus);
        if (request.getData().getTrackingNumber() != null) {
            shipment.setTrackingNumber(request.getData().getTrackingNumber());
        }
        if (request.getData().getTrackingUrl() != null) {
            shipment.setTrackingUrl(request.getData().getTrackingUrl());
        }
        geliverShipmentEntityRepository.save(shipment);

        // Save shipment event
        saveShipmentEvent(shipment, request.getData());

        // Update Order status if necessary
        orderRepository.findByOrderNumber(shipment.getOrderNumber()).ifPresent(order -> {
            updateOrderStatus(order, internalStatus);
            orderRepository.save(order);
            triggerN8NEmail(order, shipment, internalStatus, request.getData());
        });
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
            case "TRACKING_CODE_CREATED", "CREATED" -> ShipmentStatus.CREATED;
            case "PICKED_UP", "IN_TRANSIT", "SHIPPED" -> ShipmentStatus.SHIPPED;
            case "DELIVERED" -> ShipmentStatus.DELIVERED;
            case "RETURNED" -> ShipmentStatus.RETURNED;
            case "CANCELLED" -> ShipmentStatus.CANCELLED;
            default -> ShipmentStatus.PENDING;
        };
    }

    private void updateOrderStatus(Order order, ShipmentStatus shipmentStatus) {
        switch (shipmentStatus) {
            case SHIPPED -> order.setStatus(OrderStatus.SHIPPED);
            case DELIVERED -> order.setStatus(OrderStatus.DELIVERED);
            case RETURNED -> order.setStatus(OrderStatus.RETURNED);
            case CANCELLED -> order.setStatus(OrderStatus.CANCELLED);
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
        payload.put("carrier", data.getCarrier());
        payload.put("trackingUrl", data.getTrackingUrl());
        payload.put("labelUrl", shipment.getLabelUrl());

        switch (status) {
            case CREATED -> webhookUrl = n8NProperties.getWebhook().getShipmentCreated();
            case SHIPPED -> webhookUrl = n8NProperties.getWebhook().getShipmentShipped();
            case DELIVERED -> {
                webhookUrl = n8NProperties.getWebhook().getShipmentDelivered();
                payload.put("deliveryDate", data.getDeliveryDate());
            }
            case RETURNED -> {
                webhookUrl = n8NProperties.getWebhook().getShipmentReturned();
                payload.put("returnDetails", data.getReturnDetails());
            }
            default -> {
                if (data.getStatusCode() != null && data.getStatusCode().contains("FAILED")) {
                    webhookUrl = n8NProperties.getWebhook().getShipmentFailed();
                    payload.put("failureReason", data.getFailureReason());
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