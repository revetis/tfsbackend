package com.example.apps.shipments.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeliverItemRequest {

    @NotBlank(message = "Item title is required")
    private String title;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}