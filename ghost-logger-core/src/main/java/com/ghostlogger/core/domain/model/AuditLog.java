package com.ghostlogger.core.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Audit Log Record
 * <p>
 * Captures user actions, security events, and compliance data.
 * Includes userId, action type, and optional request metadata.
 */
public record AuditLog(
    UUID id,
    String message,
    String source,
    Instant timestamp,
    TraceContext traceContext,
    String userId,
    String action,
    String resourceType,
    String resourceId,
    Map<String, String> metadata
) implements LogEntry {
    
    public AuditLog {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("UserId cannot be null or blank");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be null or blank");
        }
    }
}
