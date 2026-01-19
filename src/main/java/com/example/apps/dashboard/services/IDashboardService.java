package com.example.apps.dashboard.services;

import com.example.apps.dashboard.dtos.DashboardStatsDTO;
import java.time.LocalDateTime;

public interface IDashboardService {
    DashboardStatsDTO getDashboardStats(LocalDateTime startDate, LocalDateTime endDate);
}
