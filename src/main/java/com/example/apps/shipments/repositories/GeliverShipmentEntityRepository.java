package com.example.apps.shipments.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.shipments.entities.GeliverShipmentEntity;

import com.example.apps.shipments.enums.ShipmentStatus;

@Repository
public interface GeliverShipmentEntityRepository extends JpaRepository<GeliverShipmentEntity, Long> {

    java.util.Optional<GeliverShipmentEntity> findByOrderNumber(String orderNumber);

    java.util.Optional<GeliverShipmentEntity> findByGeliverShipmentId(String geliverShipmentId);

    long countByStatus(ShipmentStatus status);
}
