package com.example.apps.payments.gateways.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.orders.entities.Order;

import com.example.apps.orders.exceptions.OrderException;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.payments.dtos.BasketItemDTO;
import com.example.apps.payments.dtos.PaymentRequestDTO;
import com.example.apps.payments.dtos.PaymentResponseDTO;
import com.example.apps.payments.entities.Payment;
import com.example.apps.payments.enums.PaymentStatus;
import com.example.apps.payments.exceptions.IyzicoPaymentCreateException;
import com.example.apps.payments.exceptions.IyzicoPaymentException;
import com.example.apps.payments.gateways.IGateway;
import com.example.apps.payments.gateways.utils.GatewayResult;
import com.example.apps.payments.gateways.utils.GatewayUtils;
import com.example.apps.payments.repositories.PaymentRepository;
import com.example.tfs.ApplicationProperties;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.Cancel;
import com.iyzipay.model.CheckoutForm;
import com.iyzipay.model.Locale;
import com.iyzipay.model.PayWithIyzicoInitialize;
import com.iyzipay.model.PayWithIyzicoInitializeRequest;
import com.iyzipay.model.Refund;
import com.iyzipay.model.Status;
import com.iyzipay.request.CreateCancelRequest;
import com.iyzipay.request.CreateRefundV2Request;
import com.iyzipay.request.RetrieveCheckoutFormRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IyzicoGateway implements IGateway {

    private final PaymentRepository paymentRepository;

    String callbackURL = "rest/api/public/payments/callback/iyzico";

    private Options options;

    private ApplicationProperties applicationProperties;

    @Autowired
    private OrderRepository orderRepository;

    public IyzicoGateway(ApplicationProperties applicationProperties, PaymentRepository paymentRepository) {
        this.applicationProperties = applicationProperties;
        this.options = new Options();
        this.options.setApiKey(applicationProperties.getIYZICO_API_KEY());
        this.options.setSecretKey(applicationProperties.getIYZICO_SECRET_KEY());
        this.options.setBaseUrl(applicationProperties.getIYZICO_BASE_URL());
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public PaymentResponseDTO initializePayment(PaymentRequestDTO request) {
        try {
            PayWithIyzicoInitializeRequest iyzicoRequest = new PayWithIyzicoInitializeRequest();
            iyzicoRequest.setLocale(Locale.TR.getValue());
            iyzicoRequest.setConversationId(GatewayUtils.generateConversationId(request.getOrderNumber()));
            iyzicoRequest.setPrice(request.getPrice());
            iyzicoRequest.setBasketId(request.getBasketId());
            iyzicoRequest.setPaymentGroup("PRODUCT");
            iyzicoRequest.setCallbackUrl(applicationProperties.getURL() + callbackURL);
            iyzicoRequest.setCurrency(request.getCurrency().toString());
            iyzicoRequest.setPaidPrice(request.getPaidPrice());
            iyzicoRequest.setPaymentSource("WEB");
            iyzicoRequest.setEnabledInstallments(request.getInstallment());
            Buyer buyer = new Buyer();
            buyer.setId(request.getBuyer().getId());
            buyer.setName(request.getBuyer().getName());
            buyer.setSurname(request.getBuyer().getSurname());
            buyer.setIdentityNumber("11111111111");
            buyer.setEmail(request.getBuyer().getEmail());
            buyer.setGsmNumber(request.getBuyer().getGsmNumber());
            buyer.setRegistrationAddress(request.getBuyer().getRegistrationAddress());
            buyer.setCity(request.getBuyer().getCity());
            buyer.setCountry(request.getBuyer().getCountry());
            buyer.setZipCode(request.getBuyer().getZipCode());
            buyer.setIp(request.getBuyer().getIp());
            // Set registration and last login dates for fraud detection
            if (request.getBuyer().getRegistrationDate() != null) {
                buyer.setRegistrationDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(request.getBuyer().getRegistrationDate()));
            }
            if (request.getBuyer().getLastLoginDate() != null) {
                buyer.setLastLoginDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(request.getBuyer().getLastLoginDate()));
            }
            iyzicoRequest.setBuyer(buyer);
            Address shippingAddress = new Address();
            shippingAddress.setContactName(request.getShippingAddress().getContactName());
            shippingAddress.setCity(request.getShippingAddress().getCity());
            shippingAddress.setCountry(request.getShippingAddress().getCountry());
            shippingAddress.setAddress(request.getShippingAddress().getAddressLine());
            shippingAddress.setZipCode(request.getShippingAddress().getZipCode());
            iyzicoRequest.setShippingAddress(shippingAddress);

            Address billingAddress = new Address();
            billingAddress.setContactName(request.getBillingAddress().getContactName());
            billingAddress.setCity(request.getBillingAddress().getCity());
            billingAddress.setCountry(request.getBillingAddress().getCountry());
            billingAddress.setAddress(request.getBillingAddress().getAddressLine());
            billingAddress.setZipCode(request.getBillingAddress().getZipCode());
            iyzicoRequest.setBillingAddress(billingAddress);

            List<BasketItemDTO> basketItems = request.getBasketItems();
            List<BasketItem> basketItemForIyzico = new ArrayList<>();

            for (BasketItemDTO basketItem : basketItems) {
                for (int i = 0; i < basketItem.getQuantity(); i++) {
                    BasketItem item = new BasketItem();
                    // Format: OrderNumber_ProductVariantId-Index (örn: TFS20260101-123456_6-0)
                    item.setId(request.getOrderNumber() + "_" + basketItem.getId().toString() + "-" + i);
                    item.setName(basketItem.getName());
                    item.setCategory1(basketItem.getMainCategory());
                    item.setCategory2(basketItem.getSubCategory());
                    item.setItemType(basketItem.getItemType());
                    item.setPrice(basketItem.getPrice());
                    basketItemForIyzico.add(item);
                }
            }
            iyzicoRequest.setBasketItems(basketItemForIyzico);

            PayWithIyzicoInitialize payWithIyzicoInitialize = PayWithIyzicoInitialize.create(iyzicoRequest, options);

            if ("failure".equalsIgnoreCase(payWithIyzicoInitialize.getStatus())) {
                log.error("Iyzico initialization failed. Error: {}, Code: {}",
                        payWithIyzicoInitialize.getErrorMessage(), payWithIyzicoInitialize.getErrorCode());
                throw new IyzicoPaymentCreateException(payWithIyzicoInitialize.getErrorMessage());
            }

            Order order = orderRepository.findByOrderNumber(request.getOrderNumber()).orElseThrow(
                    () -> new OrderException("Order not found for: " + request.getOrderNumber()));

            order.setPaymentConversationId(iyzicoRequest.getConversationId());

            orderRepository.save(order);

            return PaymentResponseDTO.builder()
                    .orderNumber(request.getOrderNumber())
                    .basketId(request.getBasketId())
                    .paymentStatus(PaymentStatus.PENDING.toString())
                    .iyzicoPageUrl(payWithIyzicoInitialize.getPayWithIyzicoPageUrl())
                    .token(payWithIyzicoInitialize.getToken())
                    .totalPrice(request.getPrice())
                    .currency(com.example.apps.payments.enums.Currency.TRY.toString())
                    .build();
        } catch (Exception e) {
            log.atInfo().setCause(e).log("Iyzico payment initialization failed: " + e.getMessage());
            throw new IyzicoPaymentCreateException("Iyzico payment initialization failed: " + e.getMessage(), e);
        }

    }

    @Override
    public GatewayResult retrievePaymentDetails(String token, String conversationId) {

        RetrieveCheckoutFormRequest req = new RetrieveCheckoutFormRequest();
        req.setToken(token);
        req.setConversationId(conversationId);
        req.setLocale(Locale.TR.getValue());

        CheckoutForm res = CheckoutForm.retrieve(req, options);

        if (!"success".equalsIgnoreCase(res.getStatus())) {
            return new GatewayResult(
                    PaymentStatus.FAILED,
                    null,
                    res.getErrorMessage());
        }

        // Critical Fix: Check the actual payment status, not just the API request
        // status
        if (!"SUCCESS".equalsIgnoreCase(res.getPaymentStatus())) {
            return new GatewayResult(
                    PaymentStatus.FAILED,
                    res.getPaymentId(),
                    res.getErrorMessage() != null ? res.getErrorMessage()
                            : "Payment failed with status: " + res.getPaymentStatus());
        }

        return new GatewayResult(PaymentStatus.SUCCESS, res.getPaymentId(), null, res.getBinNumber(),
                res.getCardAssociation(), res.getCardFamily(), res.getCardType());

    }

    @Override
    public GatewayResult cancelPayment(String paymentId, String conversationId, String ip) {

        // Validations are handled in Service layer
        // Payment payment = paymentRepository.findByPaymentId(paymentId)...

        CreateCancelRequest request = new CreateCancelRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(conversationId);
        request.setPaymentId(paymentId);
        request.setIp(ip);

        try {
            Cancel cancel = Cancel.create(request, options);

            if (Status.SUCCESS.getValue().equals(cancel.getStatus())) {
                log.info("Iyzico cancel successful for PaymentId: {}", paymentId);
                return new GatewayResult(PaymentStatus.SUCCESS, paymentId, null);
            } else {
                log.error("Iyzico cancel failed. Error: {}", cancel.getErrorMessage());
                return new GatewayResult(PaymentStatus.FAILED, paymentId, cancel.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Iyzico cancel exception", e);
            return new GatewayResult(PaymentStatus.FAILED, paymentId, e.getMessage());
        }
    }

    @Override
    public GatewayResult refundPayment(String paymentId, String conversationId, String ip, BigDecimal amount) {
        // Validations are handled in Service layer
        // Payment payment = paymentRepository.findByPaymentId(paymentId)...

        CreateRefundV2Request request = new CreateRefundV2Request();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(conversationId);
        request.setPaymentId(paymentId);
        request.setIp(ip);
        request.setPrice(amount);

        try {
            Refund refund = Refund.createV2(request, options);

            if (Status.SUCCESS.getValue().equals(refund.getStatus())) {
                log.info("Iyzico refund successful for PaymentId: {}", paymentId);
                return new GatewayResult(PaymentStatus.SUCCESS, paymentId, null);
            } else {
                log.error("Iyzico refund failed. Error: {}", refund.getErrorMessage());
                return new GatewayResult(PaymentStatus.FAILED, paymentId, refund.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Iyzico refund exception", e);
            return new GatewayResult(PaymentStatus.FAILED, paymentId, e.getMessage());
        }
    }

    @Override
    public String getGatewayName() {
        return "IYZICO";
    }

}
