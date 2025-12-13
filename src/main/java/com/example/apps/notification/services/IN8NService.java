package com.example.apps.notification.services;

import java.util.Map;

public interface IN8NService {
    void triggerWorkflow(String webhookUrl, Map<String, Object> payload);
}
