package com.example.apps.notifications.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(prefix = "tfs.n8n")
@Data
@NoArgsConstructor
public class N8NProperties {

    private String apiKey;
    private String baseUrl;
    private Webhook webhook;

    @Data
    public static class Webhook {
        private String orderConfirmation;
        private String paymentSuccess;
        private String invoice;
        private String shipmentCreated;
        private String shipmentDelivered;
        private String shipmentShipped;
        private String shipmentReturned;
        private String shipmentFailed;
        private String orderCancelled;
        private String refundProcessed;
        private String contactReply;
        private String guestReturnRequest;
        private String receipt;
        private String twoFaLogin;
        private String twoFaCode;
        private String newsletterSubscription;
        private String newsletterCampaign;
        private String signUp;
        private String forgotPassword;
        private String verifyEmail;
    }
}
