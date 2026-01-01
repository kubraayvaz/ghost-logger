package com.ghostlogger.core.domain.service;

import com.ghostlogger.core.domain.model.AuditLog;
import com.ghostlogger.core.domain.model.ErrorLog;
import com.ghostlogger.core.domain.model.LogEntry;
import com.ghostlogger.core.domain.model.MetricLog;
import com.ghostlogger.core.domain.model.TraceContext;
import com.ghostlogger.core.domain.port.LogRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Log Ingestion Service - Domain Layer Business Logic
 * <p>
 * Handles batch ingestion of log entries with:
 * - Pattern matching on sealed interfaces for type-safe processing
 * - Rate limiting via Resilience4j to protect against overload
 * - Virtual Threads for high-throughput concurrent processing
 * - TraceContext propagation via ScopedValue
 * <p>
 * Using constructor injection (final fields) for better testability - no @Autowired on fields.
 */
@Service
public class LogIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LogIngestionService.class);
    private final LogRepository logRepository;
    private final StructuredLogProcessor structuredLogProcessor;

    public LogIngestionService(final LogRepository logRepository, final StructuredLogProcessor structuredLogProcessor) {
        this.logRepository = logRepository;
        this.structuredLogProcessor = structuredLogProcessor;
        log.info("LogIngestionService initialized with repository: {}", logRepository.getClass().getSimpleName());
    }

    /**
     * Ingest a batch of log entries
     * <p>
     * Architectural Decisions:
     * - @RateLimiter protects the system from burst traffic
     * - Uses StructuredTaskScope for parallel processing with fail-fast behavior
     * - TraceContext is automatically propagated via ScopedValue
     *
     * @param logEntries List of log entries to ingest
     * @return Number of successfully ingested logs
     */
    @RateLimiter(name = "logIngestion", fallbackMethod = "rateLimitFallback")
    public int ingestBatch(final List<LogEntry> logEntries) {
        if (logEntries == null || logEntries.isEmpty()) {
            log.warn("Received empty log batch");
            return 0;
        }
        
        TraceContext currentContext = TraceContext.current();
        log.info("Ingesting batch of {} logs [traceId={}]", 
            logEntries.size(), currentContext.traceId());
        
        try {
            // Use StructuredTaskScope for parallel processing with fail-fast
            int processed = structuredLogProcessor.processBatch(logEntries);
            
            log.info("Successfully ingested {} logs [traceId={}]", 
                processed, currentContext.traceId());
            
            return processed;
        } catch (Exception e) {
            log.error("Failed to process log batch [traceId={}]: {}", 
                currentContext.traceId(), e.getMessage(), e);
            throw new RuntimeException("Log processing failed", e);
        }
    }

    /**
     * Rate limit fallback method
     * <p>
     * Returns 429 Too Many Requests via exception
     */
    @SuppressWarnings("unused")
    private int rateLimitFallback(final List<LogEntry> logEntries, final Throwable throwable) {
        log.error("Rate limit exceeded for log ingestion", throwable);
        throw new RateLimitExceededException(
            "Log ingestion rate limit exceeded. Please retry later."
        );
    }

    /**
     * Custom exception for rate limit exceeded
     */
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
