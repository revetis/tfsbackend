package com.example.apps.shipments.repositories;

import com.example.apps.shipments.entities.ShipmentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentEventRepository extends JpaRepository<ShipmentEventEntity, Long> {
    List<ShipmentEventEntity> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);
    List<ShipmentEventEntity> findByOrderNumberOrderByCreatedAtDesc(String orderNumber);
}

