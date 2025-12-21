package com.example.apps.payments.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.payments.entities.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByToken(String token);

    Optional<Payment> findByOrderNumber(String orderNumber);

    Optional<Payment> findByPaymentId(String paymentId);

}
