package com.ghostlogger.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Error Log Record
 * <p>
 * Captures error events with exception details, stack traces, and severity.
 * Immutable by design using Java Records.
 */
public record ErrorLog(
    UUID id,
    String message,
    String source,
    Instant timestamp,
    TraceContext traceContext,
    LogEntry.LogLevel severity,
    String exceptionType,
    String stackTrace
) implements LogEntry {
    
    public ErrorLog {
        // Compact constructor for validation
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or blank");
        }
        if (severity == null) {
            throw new IllegalArgumentException("Severity cannot be null");
        }
    }
}
