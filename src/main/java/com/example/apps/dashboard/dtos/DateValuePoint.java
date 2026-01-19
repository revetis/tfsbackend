package com.example.apps.dashboard.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateValuePoint {
    private String date; // YYYY-MM-DD
    private BigDecimal value;
    private String label; // For display, e.g., "1 Jan"
}
