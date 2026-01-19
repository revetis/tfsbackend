package com.example.apps.notifications.services;

import java.util.Map;

public interface IN8NService {
    void triggerWorkflow(String webhookUrl, Map<String, Object> payload);

    void sendGuestReturnRequest(String orderNumber, String customerName, String email, String magicLink,
            String logoUrl);
}
