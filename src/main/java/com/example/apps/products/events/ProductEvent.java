package com.example.apps.products.events;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent implements Serializable {
    private Long productId;
    private EventType type;

    public enum EventType {
        CREATE, UPDATE, DELETE
    }
}
