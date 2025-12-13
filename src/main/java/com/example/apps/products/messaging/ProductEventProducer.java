package com.example.apps.products.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.apps.products.events.ProductEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.product.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.product.routingkey}")
    private String routingKey;

    public void sendEvent(ProductEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
