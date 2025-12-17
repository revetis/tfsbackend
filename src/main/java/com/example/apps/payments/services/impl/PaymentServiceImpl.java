package com.example.apps.payments.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.payments.dtos.PaymentRequest;
import com.example.apps.payments.dtos.PaymentResponse;
import com.example.apps.payments.services.IPaymentService;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.Payment;
import com.iyzipay.model.PaymentCard;
import com.iyzipay.model.PaymentChannel;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.model.Refund;
import com.iyzipay.request.CreatePaymentRequest;
import com.iyzipay.request.CreateRefundRequest;
import com.iyzipay.request.RetrievePaymentRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final Options iyzicoOptions;

    @Override
    public PaymentResponse processPayment(Order order, PaymentRequest paymentRequest, String ipAddress) {
        try {
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setLocale(Locale.TR.getValue());
            request.setConversationId(String.valueOf(order.getId()));
            request.setPrice(order.getTotalAmount());
            request.setPaidPrice(order.getTotalAmount());
            request.setCurrency(Currency.TRY.name());
            request.setInstallment(1);
            request.setBasketId(order.getOrderNumber());
            request.setPaymentChannel(PaymentChannel.WEB.name());
            request.setPaymentGroup(PaymentGroup.PRODUCT.name());

            // Payment card
            PaymentCard paymentCard = new PaymentCard();
            paymentCard.setCardHolderName(paymentRequest.getCardHolderName());
            paymentCard.setCardNumber(paymentRequest.getCardNumber());
            paymentCard.setExpireMonth(paymentRequest.getExpireMonth());
            paymentCard.setExpireYear(paymentRequest.getExpireYear());
            paymentCard.setCvc(paymentRequest.getCvc());
            paymentCard.setRegisterCard(0);
            request.setPaymentCard(paymentCard);

            // Buyer
            Buyer buyer = new Buyer();
            buyer.setId(String.valueOf(order.getUser().getId()));
            buyer.setName(paymentRequest.getBuyerName());
            buyer.setSurname(paymentRequest.getBuyerSurname());
            buyer.setGsmNumber(paymentRequest.getBuyerPhone());
            buyer.setEmail(paymentRequest.getBuyerEmail());
            buyer.setIdentityNumber(paymentRequest.getBuyerIdentityNumber());
            buyer.setRegistrationAddress(paymentRequest.getBuyerAddress());
            buyer.setIp(ipAddress); // Should be actual user IP
            buyer.setCity(paymentRequest.getBuyerCity());
            buyer.setCountry(paymentRequest.getBuyerCountry());
            buyer.setZipCode(paymentRequest.getBuyerZipCode());
            request.setBuyer(buyer);

            // Shipping address
            Address shippingAddress = new Address();
            shippingAddress.setContactName(paymentRequest.getBuyerName() + " " + paymentRequest.getBuyerSurname());
            shippingAddress.setCity(paymentRequest.getBuyerCity());
            shippingAddress.setCountry(paymentRequest.getBuyerCountry());
            shippingAddress.setAddress(order.getShippingAddress());
            shippingAddress.setZipCode(paymentRequest.getBuyerZipCode());
            request.setShippingAddress(shippingAddress);

            // Billing address
            Address billingAddress = new Address();
            billingAddress.setContactName(paymentRequest.getBuyerName() + " " + paymentRequest.getBuyerSurname());
            billingAddress.setCity(paymentRequest.getBuyerCity());
            billingAddress.setCountry(paymentRequest.getBuyerCountry());
            billingAddress.setAddress(order.getBillingAddress());
            billingAddress.setZipCode(paymentRequest.getBuyerZipCode());
            request.setBillingAddress(billingAddress);

            // Basket items
            List<BasketItem> basketItems = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                BasketItem basketItem = new BasketItem();
                basketItem.setId(String.valueOf(item.getProduct().getId()));
                basketItem.setName(item.getProduct().getName());
                basketItem.setCategory1("Product");
                basketItem.setItemType(BasketItemType.PHYSICAL.name());
                basketItem.setPrice(item.getTotalPrice());
                basketItems.add(basketItem);
            }
            request.setBasketItems(basketItems);

            // Make payment
            Payment payment = Payment.create(request, iyzicoOptions);

            log.info("iyzico Payment Response: status={}, paymentId={}, conversationId={}",
                    payment.getStatus(), payment.getPaymentId(), payment.getConversationId());

            if ("success".equals(payment.getStatus())) {
                return PaymentResponse.builder()
                        .status("PAID")
                        .paymentId(payment.getPaymentId())
                        .transactionId(payment.getPaymentId())
                        .conversationId(payment.getConversationId())
                        .build();
            } else {
                return PaymentResponse.builder()
                        .status("FAILED")
                        .errorMessage(payment.getErrorMessage())
                        .conversationId(payment.getConversationId())
                        .build();
            }

        } catch (Exception e) {
            log.error("Error processing payment for order: {}", order.getId(), e);
            return PaymentResponse.builder()
                    .status("FAILED")
                    .errorMessage("Payment processing error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentResponse refundPayment(String paymentTransactionId, BigDecimal amount) {
        try {
            CreateRefundRequest request = new CreateRefundRequest();
            request.setLocale(Locale.TR.getValue());
            request.setConversationId("refund-" + System.currentTimeMillis());
            request.setPaymentTransactionId(paymentTransactionId);
            request.setPrice(amount);
            request.setIp("85.34.78.112");
            request.setCurrency(Currency.TRY.name());

            Refund refund = Refund.create(request, iyzicoOptions);

            log.info("iyzico Refund Response: status={}, paymentId={}",
                    refund.getStatus(), refund.getPaymentId());

            if ("success".equals(refund.getStatus())) {
                return PaymentResponse.builder()
                        .status("REFUNDED")
                        .paymentId(refund.getPaymentId())
                        .build();
            } else {
                return PaymentResponse.builder()
                        .status("FAILED")
                        .errorMessage(refund.getErrorMessage())
                        .build();
            }

        } catch (Exception e) {
            log.error("Error processing refund for payment: {}", paymentTransactionId, e);
            return PaymentResponse.builder()
                    .status("FAILED")
                    .errorMessage("Refund processing error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentResponse getPaymentStatus(String paymentId) {
        try {
            RetrievePaymentRequest request = new RetrievePaymentRequest();
            request.setLocale(Locale.TR.getValue());
            request.setConversationId("status-check-" + System.currentTimeMillis());
            request.setPaymentId(paymentId);

            Payment payment = Payment.retrieve(request, iyzicoOptions);

            if ("success".equals(payment.getStatus())) {
                return PaymentResponse.builder()
                        .status("PAID")
                        .paymentId(payment.getPaymentId())
                        .build();
            } else {
                return PaymentResponse.builder()
                        .status("FAILED")
                        .errorMessage(payment.getErrorMessage())
                        .build();
            }

        } catch (Exception e) {
            log.error("Error retrieving payment status: {}", paymentId, e);
            return PaymentResponse.builder()
                    .status("FAILED")
                    .errorMessage("Status check error: " + e.getMessage())
                    .build();
        }
    }
}
