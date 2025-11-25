package com.example.projectmanagement.audit.aspect;

import com.example.projectmanagement.audit.annotation.AuditLog;
import com.example.projectmanagement.audit.base.AuditTrail;
import com.example.projectmanagement.audit.base.AuditTrailRepository;
import com.example.projectmanagement.audit.dynamic.DynamicAuditService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {

    private final AuditTrailRepository auditTrailRepository;
    private final DynamicAuditService dynamicAuditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditAspect(AuditTrailRepository auditTrailRepository,
                       DynamicAuditService dynamicAuditService) {
        this.auditTrailRepository = auditTrailRepository;
        this.dynamicAuditService = dynamicAuditService;
    }

    // ---- Method-level annotation ----
    @Around("@annotation(auditLog)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        return processAudit(joinPoint, auditLog);
    }

    // ---- Class-level annotation ----
    @Around("@within(auditLog)")
    public Object auditClass(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        return processAudit(joinPoint, auditLog);
    }

    private Object processAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {

        Object response = joinPoint.proceed();

        // Convert response to JSON safely
        String newDataJson = null;
        try {
            if (response != null) {
                newDataJson = objectMapper.writeValueAsString(response);
            }
        } catch (Exception e) {
            newDataJson = "\"serialization_error\"";
        }

        // Extract HTTP Request
        ServletRequestAttributes attrs = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = attrs != null ? attrs.getRequest() : null;

        String method = req != null ? req.getMethod() : "UNKNOWN";

        // Determine operation type
        String operation = switch (method.toUpperCase()) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "UNKNOWN";
        };

        // Build audit log entry
        AuditTrail log = AuditTrail.builder()
                .entityName(auditLog.entity())
                .entityId(0L)  // You can update this later to real ID
                .operation(operation)
                .userId(1L)    // Replace with actual logged-in user ID
                .oldData(null)
                .newData(newDataJson)
                .endpoint(req != null ? req.getRequestURI() : null)
                .ipAddress(req != null ? req.getRemoteAddr() : null)
                .host(req != null ? req.getRemoteHost() : null)
                .timestamp(LocalDateTime.now())
                .build();

        auditTrailRepository.save(log);

        dynamicAuditService.saveEntityAudit(
                auditLog.entity(),
                "0",
                log.getOldData(),
                log.getNewData(),
                operation
        );

        return response;
    }
}
