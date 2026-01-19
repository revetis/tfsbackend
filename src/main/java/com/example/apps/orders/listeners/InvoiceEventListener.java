package com.example.apps.orders.listeners;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.enums.OrderStatus;
import com.example.apps.orders.events.InvoiceUploadedEvent;
import com.example.apps.orders.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventListener {

    private final IN8NService n8nService;
    private final N8NProperties n8nProperties;
    private final OrderRepository orderRepository;

    @Async
    @EventListener
    @Transactional
    public void handleInvoiceUploadedEvent(InvoiceUploadedEvent event) {
        Order eventOrder = event.getOrder();
        String invoiceUrl = event.getInvoiceUrl();

        log.info("Handling InvoiceUploadedEvent for Order: {}", eventOrder.getOrderNumber());

        try {
            // Re-fetch order to ensure attached state if needed, or mostly to handle
            // transaction if updates needed
            // But we already updated order in Service. Status update can be done here or in
            // Service.
            // Service updated fields, but Status update to COMPLETED is better here to
            // signify process end.

            Order order = orderRepository.findById(eventOrder.getId()).orElse(eventOrder);

            // Construct N8N Payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderNumber", order.getOrderNumber());
            payload.put("invoiceUrl", invoiceUrl);
            payload.put("customerName", order.getCustomerName());
            payload.put("customerEmail", order.getCustomerEmail());
            payload.put("totalAmount", order.getTotalAmount());
            payload.put("invoiceDate", order.getInvoiceUploadedAt()); // Passed date

            // Trigger N8N
            n8nService.triggerWorkflow(n8nProperties.getWebhook().getInvoice(), payload);

            log.info("N8N Invoice Workflow triggered successfully for Order: {}", order.getOrderNumber());

            // Update Status if currently WAITING_FOR_INVOICE
            if (order.getStatus() == OrderStatus.WAITING_FOR_INVOICE) {
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
                log.info("Order {} status updated to COMPLETED.", order.getOrderNumber());
            }

        } catch (Exception e) {
            log.error("Failed to process InvoiceUploadedEvent for Order: {}", eventOrder.getOrderNumber(), e);
            // Ideally we should have a retry mechanism or Dead Letter Queue here
        }
    }
}
