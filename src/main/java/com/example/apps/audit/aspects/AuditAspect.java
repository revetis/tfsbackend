package com.example.apps.audit.aspects;

import com.example.apps.audit.annotations.Auditable;
import com.example.apps.audit.services.AuditLogService;
import com.example.tfs.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditable)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String errorDetails = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            status = "FAILURE";
            errorDetails = e.getMessage();
            throw e;
        } finally {
            try {
                Long userId = SecurityUtils.getCurrentUserIdOrNull();
                String ipAddress = getClientIp();
                String action = auditable.action();

                String details = "Args: " + maskArgs(joinPoint.getArgs());
                if (errorDetails != null) {
                    details += " | Error: " + errorDetails;
                }

                // Simple entity name/id extraction could go here, for now using method name or
                // simplistic approach
                String entityName = joinPoint.getSignature().getDeclaringType().getSimpleName();
                String entityId = null; // Hard to extract generically without more annotations

                auditLogService.log(
                        userId,
                        action,
                        entityName,
                        entityId,
                        details,
                        ipAddress,
                        status);
            } catch (Exception ex) {
                log.error("Audit logging failed", ex);
            }
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0];
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Ignore for non-web contexts
        }
        return "UNKNOWN";
    }

    private String maskArgs(Object[] args) {
        if (args == null)
            return "null";
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null)
                        return "null";
                    String str = arg.toString();
                    // Basic masking for known sensitive keys in standard toString() formats (e.g.
                    // Lombok)
                    // Matches patterns like "password=secret" or "password='secret'" or
                    // "password":"secret"
                    return str.replaceAll("(?i)(password|token|cvv|secret)(\\s*[:=]\\s*['\"]?)([^,\\s'\"]+)(['\"]?)",
                            "$1$2*****$4");
                })
                .collect(java.util.stream.Collectors.toList()).toString();
    }
}
