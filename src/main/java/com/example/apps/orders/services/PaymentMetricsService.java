package com.example.apps.orders.services;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentMetricsService {

    private final MeterRegistry meterRegistry;

    public void recordRefundAttempt(boolean success) {
        Counter.builder("refund.attempts")
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .increment();
    }

    public void recordRefundDuration(long durationMs) {
        Timer.builder("refund.duration")
                .description("Time taken to process refund")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordWebhookReceived(String eventType) {
        Counter.builder("webhook.received")
                .tag("type", eventType)
                .register(meterRegistry)
                .increment();
    }

    public void recordWebhookProcessed(String eventType, boolean success) {
        Counter.builder("webhook.processed")
                .tag("type", eventType)
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .increment();
    }

    public void recordOrderCancellation(boolean success) {
        Counter.builder("order.cancellation")
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .increment();
    }

    public void recordWebhookProcessingDuration(String eventType, long durationMs) {
        Timer.builder("webhook.processing.duration")
                .description("Time taken to process incoming webhooks")
                .tag("type", eventType)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
