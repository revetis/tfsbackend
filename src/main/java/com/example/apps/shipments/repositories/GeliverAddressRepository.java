package com.example.apps.shipments.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.shipments.entities.GeliverAddress;

@Repository
public interface GeliverAddressRepository extends JpaRepository<GeliverAddress, Long> {

    Optional<GeliverAddress> findByGeliverAddressId(String geliverAddressId);

    Optional<GeliverAddress> findByIsDefaultSenderTrue();

    Optional<GeliverAddress> findByIsDefaultReturnTrue();

    List<GeliverAddress> findByIsRecipient(Boolean isRecipient);

    List<GeliverAddress> findByIsActiveTrue();
}
