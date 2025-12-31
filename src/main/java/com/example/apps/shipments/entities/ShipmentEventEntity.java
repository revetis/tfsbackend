package com.example.apps.shipments.entities;

import com.example.tfs.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "shipment_events")
@Getter
@Setter
@NoArgsConstructor
public class ShipmentEventEntity extends BaseEntity {

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId; // GeliverShipmentEntity id

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "geliver_shipment_id")
    private String geliverShipmentId;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "status_code")
    private String statusCode; // Geliver status code (CREATED, SHIPPED, DELIVERED, etc.)

    @Column(name = "tracking_status_code")
    private String trackingStatusCode; // PRE_TRANSIT, TRANSIT, DELIVERED, etc.

    @Column(name = "tracking_sub_status_code")
    private String trackingSubStatusCode;

    @Column(name = "status_details", columnDefinition = "TEXT")
    private String statusDetails;

    @Column(name = "status_date")
    private OffsetDateTime statusDate;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lng")
    private Double locationLng;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Column(name = "delivery_date")
    private String deliveryDate;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "return_details", columnDefinition = "TEXT")
    private String returnDetails;
}

