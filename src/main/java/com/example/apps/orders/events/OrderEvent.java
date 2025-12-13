package com.example.apps.orders.events;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private EventType type;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Integer retryCount = 0;

    public OrderEvent(Long orderId, EventType type) {
        this.orderId = orderId;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.retryCount = 0;
    }

    public enum EventType {
        CREATE, UPDATE, CANCEL, REFUND
    }
}
