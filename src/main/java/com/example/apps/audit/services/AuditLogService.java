package com.example.apps.audit.services;

import com.example.apps.audit.entities.AuditLog;
import com.example.apps.audit.repositories.AuditLogRepository;
import com.example.tfs.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String entityName, String entityId, String details, String ipAddress,
            String status) {
        try {
            AuditLog logEntry = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .status(status)
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
}
