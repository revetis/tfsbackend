package com.example.apps.orders.services;

import java.math.BigDecimal;

import com.example.apps.orders.dtos.PaymentRequest;
import com.example.apps.orders.dtos.PaymentResponse;
import com.example.apps.orders.entities.Order;

public interface IPaymentService {

    PaymentResponse processPayment(Order order, PaymentRequest paymentRequest);

    PaymentResponse refundPayment(String paymentTransactionId, BigDecimal amount);

    PaymentResponse getPaymentStatus(String paymentId);
}
