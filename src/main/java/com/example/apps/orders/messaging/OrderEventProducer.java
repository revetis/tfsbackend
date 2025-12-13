package com.example.apps.orders.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.apps.orders.events.OrderEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.order.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.order.routingkey}")
    private String routingKey;

    public void sendEvent(OrderEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published OrderEvent: orderId={}, type={}", event.getOrderId(), event.getType());
        } catch (Exception e) {
            log.error("Failed to publish OrderEvent: orderId={}, type={}", event.getOrderId(), event.getType(), e);
            throw new RuntimeException("Failed to publish order event", e);
        }
    }
}
