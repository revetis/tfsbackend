package com.example.apps.dashboard.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusDistributionDTO {
    private String name;
    private Long value;
    private String color;
}
