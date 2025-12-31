package com.example.apps.orders.controllers;

import com.example.apps.orders.dtos.CreateReturnRequestDTO;
import com.example.apps.orders.dtos.ReturnRequestResponseDTO;
import com.example.apps.orders.services.IReturnService;
import com.example.tfs.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/private/returns")
@RequiredArgsConstructor
@Tag(name = "Return Management (User)", description = "Operations for users to manage return requests")
public class ReturnController {

    private final IReturnService returnService;

    @PostMapping
    @Operation(summary = "Create a return request")
    public ResponseEntity<ReturnRequestResponseDTO> createReturnRequest(@RequestBody CreateReturnRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(returnService.createReturnRequest(userId, request));
    }

    @GetMapping
    @Operation(summary = "Get my return requests")
    public ResponseEntity<List<ReturnRequestResponseDTO>> getMyReturns() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(returnService.getUserReturnRequests(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get return request details")
    public ResponseEntity<ReturnRequestResponseDTO> getReturnRequest(@PathVariable Long id) {
        // TODO: Add security check to ensure user owns this request
        // Implementing basic check in service or here? service checked creation
        // ownership.
        // Assuming secure by ID lookup for now or relying on service/repository
        // filtering.
        // Actually service `getReturnRequestById` is generic. Ideally we check userId
        // match here.
        ReturnRequestResponseDTO dto = returnService.getReturnRequestById(id);
        Long userId = SecurityUtils.getCurrentUserId();
        if (!dto.getUserId().equals(userId)) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(dto);
    }
}
