package com.example.apps.orders.services;

import com.example.apps.orders.dtos.CreateReturnRequestDTO;
import com.example.apps.orders.dtos.ReturnRequestResponseDTO;
import com.example.apps.orders.enums.ReturnRequestStatus;

import java.util.List;

public interface IReturnService {

    ReturnRequestResponseDTO createReturnRequest(Long userId, CreateReturnRequestDTO request);

    ReturnRequestResponseDTO getReturnRequestById(Long id);

    List<ReturnRequestResponseDTO> getUserReturnRequests(Long userId);

    List<ReturnRequestResponseDTO> getAllReturnRequests();

    List<ReturnRequestResponseDTO> getReturnRequestsByStatus(ReturnRequestStatus status);

    ReturnRequestResponseDTO markAsReceived(Long requestId);

    ReturnRequestResponseDTO approveReturnRequest(Long requestId, boolean restockItems);

    ReturnRequestResponseDTO rejectReturnRequest(Long requestId, String reason);
}
