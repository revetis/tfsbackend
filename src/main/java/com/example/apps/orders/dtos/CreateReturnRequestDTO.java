package com.example.apps.orders.dtos;

import com.example.apps.orders.enums.ReturnReason;
import lombok.Data;
import java.util.List;

@Data
public class CreateReturnRequestDTO {
    private Long orderId;
    private ReturnReason returnReason;
    private String description;
    private List<ReturnItemRequestDTO> items;
}
