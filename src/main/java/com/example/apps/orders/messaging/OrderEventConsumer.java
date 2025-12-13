package com.example.apps.orders.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.orders.documents.OrderDocument;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.events.OrderEvent;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.orders.repositories.search.OrderSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final OrderSearchRepository orderSearchRepository;

    @RabbitListener(queues = "${app.rabbitmq.order.queue}")
    @Transactional
    public void consumeEvent(OrderEvent event) {
        log.info("Received OrderEvent: orderId={}, type={}, retryCount={}",
                event.getOrderId(), event.getType(), event.getRetryCount());

        try {
            if (event.getType() == OrderEvent.EventType.CANCEL) {
                orderSearchRepository.deleteById(event.getOrderId());
                log.info("Deleted order from ES: {}", event.getOrderId());
                return;
            }

            Order order = orderRepository.findById(event.getOrderId())
                    .orElse(null);

            if (order == null) {
                log.warn("Order not found in DB for event: {}", event);
                orderSearchRepository.deleteById(event.getOrderId());
                return;
            }

            OrderDocument document = mapToDocument(order);
            orderSearchRepository.save(document);
            log.info("Indexed order in ES: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error processing OrderEvent: orderId={}, type={}, retry={}",
                    event.getOrderId(), event.getType(), event.getRetryCount(), e);
            // Rethrow to trigger RabbitMQ retry and eventually DLQ
            throw new RuntimeException("Failed to process order event", e);
        }
    }

    private OrderDocument mapToDocument(Order order) {
        return OrderDocument.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
