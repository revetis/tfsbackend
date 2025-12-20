package com.example.apps.payments.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.payments.dtos.PaymentRequestDTO;
import com.example.apps.payments.dtos.PaymentResponseDTO;
import com.example.apps.payments.entities.Payment;
import com.example.apps.payments.entities.PaymentAddress;
import com.example.apps.payments.entities.PaymentBasketItem;
import com.example.apps.payments.entities.PaymentBuyer;
import com.example.apps.payments.entities.PaymentTransaction;
import com.example.apps.payments.enums.PaymentStatus;
import com.example.apps.payments.exceptions.NoPaymentGatewayFoundException;
import com.example.apps.payments.exceptions.PaymentRecordNotFoundForTokenException;
import com.example.apps.payments.gateways.IGateway;
import com.example.apps.payments.repositories.PaymentRepository;
import com.example.apps.payments.services.IPaymentService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;

    private final Map<String, IGateway> gateways;

    public PaymentServiceImpl(List<IGateway> gatewayList, PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(IGateway::getGatewayName, g -> g));
    }

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        IGateway selectedGateway = gateways.get(request.getSelectedGateway());
        if (selectedGateway == null) {
            throw new NoPaymentGatewayFoundException("No payment gateway found for: " + request.getSelectedGateway());
        }
        log.info("Creating payment for order number: {}", request.getOrderNumber());

        Payment payment = new Payment();
        payment.setOrderNumber(request.getOrderNumber());
        payment.setTotalPrice(request.getPrice());
        payment.setPaidPrice(request.getPaidPrice());
        payment.setCurrency(request.getCurrency());
        payment.setInstallments(request.getInstallment());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSelectedGateway(request.getSelectedGateway());

        PaymentBuyer buyer = new PaymentBuyer();
        buyer.setBuyerId(request.getBuyer().getId());
        buyer.setName(request.getBuyer().getName());
        buyer.setSurname(request.getBuyer().getSurname());
        buyer.setEmail(request.getBuyer().getEmail());
        buyer.setIdentityNumber(request.getBuyer().getIdentityNumber());
        buyer.setGsmNumber(request.getBuyer().getGsmNumber());
        buyer.setIp(request.getBuyer().getIp());
        payment.setBuyer(buyer);

        payment.setShippingAddress(mapAddress(request.getShippingAddress()));
        payment.setBillingAddress(mapAddress(request.getBillingAddress()));

        List<PaymentBasketItem> basketItems = request.getBasketItems().stream().map(itemDTO -> {
            PaymentBasketItem item = new PaymentBasketItem();
            item.setProductVariantId(itemDTO.getId());
            item.setProductName(itemDTO.getName());
            item.setPrice(itemDTO.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            item.setMainCategory(itemDTO.getMainCategory());
            item.setSubCategory(itemDTO.getSubCategory());
            item.setItemType(itemDTO.getItemType());
            item.setPayment(payment);
            return item;
        }).collect(Collectors.toList());
        payment.setBasketItems(basketItems);

        PaymentResponseDTO gatewayResponse = selectedGateway.initializePayment(request);

        payment.setToken(gatewayResponse.getToken());
        payment.setConversationId(gatewayResponse.getConversationId());

        paymentRepository.save(payment);

        return gatewayResponse;
    }

    @Override
    @Transactional
    public PaymentResponseDTO completePayment(String token) {

        log.info("Completing payment for token: {}", token);

        Payment payment = paymentRepository.findByToken(token)
                .orElseThrow(() -> new PaymentRecordNotFoundForTokenException(
                        "Payment record not found for token: " + token));

        IGateway selectedGateway = gateways.get(payment.getSelectedGateway());

        if (selectedGateway == null) {
            throw new NoPaymentGatewayFoundException("No payment gateway found for: " + payment.getSelectedGateway());
        }

        PaymentResponseDTO details = selectedGateway.retrievePaymentDetails(token, payment.getConversationId());
        String rawResponse = details.getTransactions().stream()
                .map(t -> t.getRawResponse())
                .collect(Collectors.joining("\n"));

        payment.setStatus(PaymentStatus.valueOf(details.getPaymentStatus()));
        payment.setPaymentId(details.getPaymentId());
        payment.setBinNumber(details.getBinNumber());
        payment.setCardFamily(details.getCardFamily());

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPayment(payment);
        transaction.setTransactionId(details.getPaymentId());
        transaction.setStatus(details.getPaymentStatus());
        transaction.setRawResponse(rawResponse.toString());

        if (payment.getTransactions() == null)
            payment.setTransactions(new ArrayList<>());
        payment.getTransactions().add(transaction);

        paymentRepository.save(payment);

        return details;
    }

    private PaymentAddress mapAddress(com.example.apps.payments.dtos.AddressDTO dto) {
        PaymentAddress address = new PaymentAddress();
        address.setContactName(dto.getContactName());
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setAddressLine(dto.getAddressLine());
        address.setZipCode(dto.getZipCode());
        return address;
    }

}