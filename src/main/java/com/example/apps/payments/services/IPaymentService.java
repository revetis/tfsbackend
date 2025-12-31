package com.example.apps.payments.services;

import com.example.apps.payments.dtos.OrderReturnRequestDTO;
import com.example.apps.payments.dtos.PaymentAdminDTO;
import com.example.apps.payments.dtos.PaymentRequestDTO;

import com.example.apps.payments.dtos.PaymentResponseDTO;

import java.util.List;

public interface IPaymentService {

    PaymentResponseDTO createPayment(PaymentRequestDTO request);

    PaymentResponseDTO completePayment(String token);

    void returnPayment(String orderNumber);

    void refundPartialPayment(OrderReturnRequestDTO returnRequest);

    List<PaymentAdminDTO> getAllPayments();

    PaymentAdminDTO getPaymentById(Long id);

    void purchaseShipmentForOrder(String orderNumber);
}
