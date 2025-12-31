package com.ghostlogger.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Sealed Interface for Log Entries (Java 21+)
 * <p>
 * Using a sealed interface enforces exhaustive pattern matching and type safety.
 * This is a modern approach to modeling polymorphic domain entities.
 * <p>
 * Only ErrorLog, AuditLog, and MetricLog are permitted implementations.
 */
public sealed interface LogEntry permits ErrorLog, AuditLog, MetricLog {

    UUID id();
    String message();
    String source();
    Instant timestamp();
    TraceContext traceContext();

    /**
     * Log Level Enumeration
     */
    enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }
}
