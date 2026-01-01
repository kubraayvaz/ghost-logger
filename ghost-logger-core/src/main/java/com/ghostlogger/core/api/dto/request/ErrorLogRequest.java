package com.ghostlogger.core.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Error Log Request DTO
 */
public record ErrorLogRequest(
    String type,
    @NotBlank(message = "Message cannot be blank")
    String message,
    
    @NotBlank(message = "Source cannot be blank")
    String source,
    
    Instant timestamp,
    
    TraceContextRequest traceContext,
    
    @NotNull(message = "Severity cannot be null")
    String severity,
    
    String exceptionType,
    
    String stackTrace
) implements LogEntryRequest {
    
    public ErrorLogRequest {
        if (type == null) {
            type = "ERROR";
        }
    }
}
