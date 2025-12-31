package com.ghostlogger.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;

/**
 * Audit Log Request DTO
 */
public record AuditLogRequest(
    String type,
    
    @NotBlank(message = "Message cannot be blank")
    String message,
    
    @NotBlank(message = "Source cannot be blank")
    String source,
    
    Instant timestamp,
    
    TraceContextRequest traceContext,
    
    @NotBlank(message = "UserId cannot be blank")
    String userId,
    
    @NotBlank(message = "Action cannot be blank")
    String action,
    
    String resourceType,
    
    String resourceId,
    
    Map<String, String> metadata
) implements LogEntryRequest {
    
    public AuditLogRequest {
        if (type == null) {
            type = "AUDIT";
        }
    }
}
