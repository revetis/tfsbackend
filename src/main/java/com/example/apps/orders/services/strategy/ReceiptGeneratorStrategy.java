package com.example.apps.orders.services.strategy;

import com.example.apps.orders.entities.Order;

public interface ReceiptGeneratorStrategy {
    byte[] generateReceipt(Order order);
}
