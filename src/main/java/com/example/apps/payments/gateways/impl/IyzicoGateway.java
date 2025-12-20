package com.example.apps.payments.gateways.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.payments.dtos.BasketItemDTO;
import com.example.apps.payments.dtos.PaymentRequestDTO;
import com.example.apps.payments.dtos.PaymentResponseDTO;
import com.example.apps.payments.enums.PaymentStatus;
import com.example.apps.payments.exceptions.IyzicoRetrievePaymentDetailsException;
import com.example.apps.payments.exceptions.IyzijoPaymentCreateException;
import com.example.apps.payments.gateways.IGateway;
import com.example.apps.payments.gateways.utils.GatewayUtils;
import com.example.tfs.ApplicationProperties;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.CheckoutForm;
import com.iyzipay.model.Locale;
import com.iyzipay.model.PayWithIyzicoInitialize;
import com.iyzipay.model.PayWithIyzicoInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IyzicoGateway implements IGateway {
    String callbackURL = "rest/api/public/payments/callback/iyzico";

    private Options options;

    private ApplicationProperties applicationProperties;

    @Autowired
    private IN8NService n8nService;

    @Autowired
    private N8NProperties n8NProperties;

    @Autowired
    private OrderRepository orderRepository;

    public IyzicoGateway(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.options = new Options();
        this.options.setApiKey(applicationProperties.getIYZICO_API_KEY());
        this.options.setSecretKey(applicationProperties.getIYZICO_SECRET_KEY());
        this.options.setBaseUrl(applicationProperties.getIYZICO_BASE_URL());
    }

    @Override
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
                    item.setId(basketItem.getId().toString());
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
            throw new IyzijoPaymentCreateException("Iyzico payment initialization failed: " + e.getMessage(), e);
        }

    }

    @Override
    public PaymentResponseDTO retrievePaymentDetails(String token, String conversationId) {
        try {
            RetrieveCheckoutFormRequest retrieveCheckoutFormRequest = new RetrieveCheckoutFormRequest();
            retrieveCheckoutFormRequest.setToken(token);
            retrieveCheckoutFormRequest.setConversationId(conversationId);
            retrieveCheckoutFormRequest.setLocale(Locale.TR.getValue());
            CheckoutForm checkoutFormResponse = CheckoutForm.retrieve(retrieveCheckoutFormRequest, options);

            if (!"success".equalsIgnoreCase(checkoutFormResponse.getStatus())) {
                log.error("Iyzico detail inquiry failed. Error: {}, Code: {}",
                        checkoutFormResponse.getErrorMessage(), checkoutFormResponse.getErrorCode());

                return PaymentResponseDTO.builder()
                        .paymentStatus(PaymentStatus.FAILED.toString())
                        .build();
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("subject", "Siparişiniz onaylandı");
            payload.put("preHeader", "Siparişiniz onaylandı");
            payload.put("faviconURL", "http://localhost:3000/favicon.ico");

            n8nService.triggerWorkflow(n8NProperties.getWebhook().getOrderConfirmation(), payload);

            return PaymentResponseDTO.builder()
                    .orderNumber(checkoutFormResponse.getBasketId())
                    .paymentId(checkoutFormResponse.getPaymentId())
                    .paymentStatus(checkoutFormResponse.getPaymentStatus())
                    .binNumber(checkoutFormResponse.getBinNumber())
                    .cardFamily(checkoutFormResponse.getCardFamily())
                    .totalPrice(checkoutFormResponse.getPrice())
                    .currency(checkoutFormResponse.getCurrency())
                    .conversationId(checkoutFormResponse.getConversationId())
                    .build();
        } catch (Exception e) {
            log.atInfo().setCause(e).log("Iyzico payment details retrieval failed: " + e.getMessage());
            throw new IyzicoRetrievePaymentDetailsException(
                    "Iyzico payment details retrieval failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getGatewayName() {
        return "IYZICO";
    }

}
