package com.example.apps.orders.events;

import com.example.apps.orders.entities.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InvoiceUploadedEvent extends ApplicationEvent {

    private final Order order;
    private final String invoiceUrl;

    public InvoiceUploadedEvent(Object source, Order order, String invoiceUrl) {
        super(source);
        this.order = order;
        this.invoiceUrl = invoiceUrl;
    }
}
