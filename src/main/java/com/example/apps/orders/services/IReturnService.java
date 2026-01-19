package com.example.apps.orders.services;

import com.example.apps.orders.dtos.CreateReturnRequestDTO;
import com.example.apps.orders.dtos.ReturnRequestResponseDTO;
import com.example.apps.orders.enums.ReturnRequestStatus;

import java.util.List;

public interface IReturnService {

        ReturnRequestResponseDTO createReturnRequest(Long userId, CreateReturnRequestDTO request);

        ReturnRequestResponseDTO createGuestReturnRequest(String orderNumber, String initiatorEmail,
                        CreateReturnRequestDTO request);

        ReturnRequestResponseDTO getReturnRequestById(Long id, Long userId);

        List<ReturnRequestResponseDTO> getUserReturnRequests(Long userId);

        org.springframework.data.domain.Page<ReturnRequestResponseDTO> getUserReturnRequests(Long userId, int page,
                        int size);

        List<ReturnRequestResponseDTO> getAllReturnRequests();

        List<ReturnRequestResponseDTO> getReturnRequestsByStatus(ReturnRequestStatus status);

        ReturnRequestResponseDTO markAsReceived(Long requestId);

        ReturnRequestResponseDTO approveReturnRequest(Long requestId, boolean restockItems);

        ReturnRequestResponseDTO rejectReturnRequest(Long requestId, String reason);

        ReturnRequestResponseDTO cancelReturnRequest(Long userId, Long requestId);

        // Admin methods with pagination and filtering
        ReturnPageResult getAllReturns(int page, int size, String sortField, String sortOrder, Long userId,
                        ReturnRequestStatus status);

        record ReturnPageResult(java.util.List<ReturnRequestResponseDTO> data, long totalCount) {
        }
}
