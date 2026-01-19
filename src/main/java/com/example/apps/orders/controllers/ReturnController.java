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
    public ResponseEntity<?> getMyReturns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(returnService.getUserReturnRequests(userId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get return request details")
    public ResponseEntity<ReturnRequestResponseDTO> getReturnRequest(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReturnRequestResponseDTO dto = returnService.getReturnRequestById(id, userId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a return request")
    public ResponseEntity<ReturnRequestResponseDTO> cancelReturnRequest(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(returnService.cancelReturnRequest(userId, id));
    }
}
