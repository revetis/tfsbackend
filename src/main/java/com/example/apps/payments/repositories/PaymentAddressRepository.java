package com.example.apps.payments.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.payments.entities.PaymentAddress;

@Repository
public interface PaymentAddressRepository extends JpaRepository<PaymentAddress, Long> {

}
