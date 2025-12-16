package com.example.apps.orders.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.orders.dtos.GeliverWebhookDTO;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.Shipment;
import com.example.apps.orders.entities.Shipment.ShipmentStatus;
import com.example.apps.orders.entities.Shipment.TrackingStatus;
import com.example.apps.orders.repositories.ShipmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeliverWebhookService {

    private final ShipmentRepository shipmentRepository;

    private final N8NNotificationService n8nService;

    @Transactional
    public void processShipmentUpdate(GeliverWebhookDTO webhook) {
        Shipment shipment = shipmentRepository
                .findByGeliverShipmentId(webhook.getShipmentId())
                .orElse(null);

        if (shipment == null) {
            log.warn("Shipment not found for webhook: {}", webhook.getShipmentId());
            return;
        }

        // Update tracking status
        shipment.setTrackingStatus(mapTrackingStatus(webhook.getTrackingStatusCode()));

        if (webhook.getTrackingNumber() != null) {
            shipment.setTrackingNumber(webhook.getTrackingNumber());
        }

        if (webhook.getTrackingUrl() != null) {
            shipment.setTrackingUrl(webhook.getTrackingUrl());
        }

        // Update shipment status based on tracking
        if ("DELIVERED".equals(webhook.getTrackingStatusCode())) {
            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipment.setDeliveredAt(webhook.getStatusDate());

            // Update order status
            Order order = shipment.getOrder();
            order.setStatus(Order.OrderStatus.DELIVERED);

            n8nService.sendShipmentDelivered(order, shipment);

            log.info("Shipment delivered: {} - Order: {}", webhook.getShipmentId(), order.getOrderNumber());
        } else if ("TRANSIT".equals(webhook.getTrackingStatusCode())) {
            if ("package_accepted".equals(webhook.getTrackingSubStatusCode())) {
                shipment.setStatus(ShipmentStatus.SHIPPED);
                shipment.setShipmentDate(webhook.getStatusDate());

                n8nService.sendShipmentShipped(shipment.getOrder(), shipment);
            }
        } else if ("RETURNED".equals(webhook.getTrackingStatusCode())) {
            shipment.setStatus(ShipmentStatus.RETURNED);

            n8nService.sendShipmentReturned(shipment.getOrder(), shipment);
        } else if ("FAILURE".equals(webhook.getTrackingStatusCode())) {
            shipment.setStatus(ShipmentStatus.FAILED);
            shipment.setHasError(true);
            shipment.setLastErrorMessage(webhook.getStatusDetails());

            n8nService.sendShipmentFailed(shipment.getOrder(), shipment);
        }

        shipmentRepository.save(shipment);
        log.info("Shipment updated: {} - Status: {}", webhook.getShipmentId(), webhook.getTrackingStatusCode());
    }

    private TrackingStatus mapTrackingStatus(String trackingStatusCode) {
        if (trackingStatusCode == null) {
            return TrackingStatus.UNKNOWN;
        }

        return switch (trackingStatusCode) {
            case "PRE_TRANSIT" -> TrackingStatus.PRE_TRANSIT;
            case "TRANSIT" -> TrackingStatus.TRANSIT;
            case "DELIVERED" -> TrackingStatus.DELIVERED;
            case "RETURNED" -> TrackingStatus.RETURNED;
            case "FAILURE" -> TrackingStatus.FAILURE;
            default -> TrackingStatus.UNKNOWN;
        };
    }
}
