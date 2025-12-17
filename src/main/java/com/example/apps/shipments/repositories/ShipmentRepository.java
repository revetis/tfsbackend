package com.example.apps.orders.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.orders.entities.Shipment;
import com.example.apps.orders.entities.Shipment.ShipmentStatus;
import com.example.apps.orders.entities.Shipment.TrackingStatus;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrderId(Long orderId);

    Optional<Shipment> findByGeliverShipmentId(String geliverShipmentId);

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findByTrackingStatus(TrackingStatus trackingStatus);

    List<Shipment> findByIsReturnTrue();
}
