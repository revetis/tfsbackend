package com.example.apps.dashboard.controllers;

import com.example.apps.dashboard.dtos.DashboardStatsDTO;
import com.example.apps.dashboard.services.IDashboardService;
import com.example.tfs.maindto.ApiTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/rest/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    @Autowired
    private IDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiTemplate<Void, DashboardStatsDTO>> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest servletRequest) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        DashboardStatsDTO stats = dashboardService.getDashboardStats(startDate, endDate);

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                200,
                servletRequest.getRequestURI(),
                null,
                stats));
    }
}
