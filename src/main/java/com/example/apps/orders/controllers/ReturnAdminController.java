package com.example.apps.orders.controllers;

import com.example.apps.orders.dtos.ApproveReturnDTO;
import com.example.apps.orders.dtos.RejectReturnDTO;
import com.example.apps.orders.dtos.ReturnRequestResponseDTO;
import com.example.apps.orders.enums.ReturnRequestStatus;
import com.example.apps.orders.services.IReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/admin/returns")
@RequiredArgsConstructor
@Tag(name = "Return Management (Admin)", description = "Admin operations for return requests")
@PreAuthorize("hasRole('ADMIN')")
public class ReturnAdminController {

    private final IReturnService returnService;

    @GetMapping
    @Operation(summary = "List all return requests")
    public ResponseEntity<List<ReturnRequestResponseDTO>> getAllReturns(
            @RequestParam(required = false) ReturnRequestStatus status) {
        if (status != null) {
            return ResponseEntity.ok(returnService.getReturnRequestsByStatus(status));
        }
        return ResponseEntity.ok(returnService.getAllReturnRequests());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get return request details")
    public ResponseEntity<ReturnRequestResponseDTO> getReturnRequest(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.getReturnRequestById(id));
    }

    @PutMapping("/{id}/received")
    @Operation(summary = "Mark return request as received at warehouse")
    public ResponseEntity<ReturnRequestResponseDTO> markAsReceived(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.markAsReceived(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve return request and trigger refund")
    public ResponseEntity<ReturnRequestResponseDTO> approveReturn(@PathVariable Long id,
            @RequestBody ApproveReturnDTO dto) {
        return ResponseEntity.ok(returnService.approveReturnRequest(id, dto.isRestock()));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject return request")
    public ResponseEntity<ReturnRequestResponseDTO> rejectReturn(@PathVariable Long id,
            @RequestBody RejectReturnDTO dto) {
        return ResponseEntity.ok(returnService.rejectReturnRequest(id, dto.getReason()));
    }
}
