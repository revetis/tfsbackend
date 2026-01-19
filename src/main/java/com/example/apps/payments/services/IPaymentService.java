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

    // Paginated version with filtering
    PaymentPageResult getAllPayments(int start, int end, String sortField, String sortOrder, String search,
            String status);

    PaymentAdminDTO getPaymentById(Long id);

    void purchaseShipmentForOrder(String orderNumber);

    // Result record for paginated payments
    record PaymentPageResult(List<PaymentAdminDTO> data, long totalCount) {
    }
}
