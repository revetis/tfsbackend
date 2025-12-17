package com.example.apps.payments.services;

import java.math.BigDecimal;

import com.example.apps.orders.entities.Order;
import com.example.apps.payments.dtos.PaymentRequest;
import com.example.apps.payments.dtos.PaymentResponse;

public interface IPaymentService {

    PaymentResponse processPayment(Order order, PaymentRequest paymentRequest, String ipAddress);

    PaymentResponse refundPayment(String paymentTransactionId, BigDecimal amount);

    PaymentResponse getPaymentStatus(String paymentId);
}
