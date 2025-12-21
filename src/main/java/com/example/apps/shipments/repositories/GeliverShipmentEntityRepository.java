package com.example.apps.shipments.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.shipments.entities.GeliverShipmentEntity;

@Repository
public interface GeliverShipmentEntityRepository extends JpaRepository<GeliverShipmentEntity, Long> {

}
