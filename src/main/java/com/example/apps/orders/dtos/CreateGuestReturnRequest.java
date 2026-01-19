package com.example.apps.orders.dtos;

import java.util.List;

import com.example.apps.orders.enums.ReturnReason;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateGuestReturnRequest {
    private String token;
    private ReturnReason returnReason;
    private String description;

    // Items to return
    private List<CreateGuestReturnItemRequest> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGuestReturnItemRequest {
        private Long orderItemId;
        private Integer quantity;
    }
}
