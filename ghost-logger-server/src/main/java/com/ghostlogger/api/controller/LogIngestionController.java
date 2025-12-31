package com.ghostlogger.api.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ghostlogger.api.dto.request.AuditLogRequest;
import com.ghostlogger.api.dto.request.ErrorLogRequest;
import com.ghostlogger.api.dto.request.LogBatchRequest;
import com.ghostlogger.api.dto.request.LogEntryRequest;
import com.ghostlogger.api.dto.request.MetricLogRequest;
import com.ghostlogger.api.dto.response.LogIngestResponse;
import com.ghostlogger.domain.model.AuditLog;
import com.ghostlogger.domain.model.ErrorLog;
import com.ghostlogger.domain.model.LogEntry;
import com.ghostlogger.domain.model.MetricLog;
import com.ghostlogger.domain.model.TraceContext;
import com.ghostlogger.domain.service.LogIngestionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Log Ingestion Controller (Contract-First REST API)
 * <p>
 * POST /logs/ingest - Accepts batch ingestion of log entries.
 * <p>
 * Architecture Rationale:
 * - Returns 202 Accepted for asynchronous processing (bulk ingestion is I/O-intensive)
 * - Uses Virtual Threads for high-throughput concurrent processing
 * - Idempotency can be added via Idempotency-Key header if needed
 * - Rate limiting is handled via Resilience4j at the service layer
 * <p>
 * Richardson Maturity Level 2: HTTP verbs + status codes
 */
@RestController
@RequestMapping("/api/v1/logs")
@Tag(name = "Log Ingestion", description = "High-Performance Log Ingestion Endpoints")
public final class LogIngestionController {

    private static final Logger logger = LoggerFactory.getLogger(LogIngestionController.class);

    private final LogIngestionService logIngestionService;

    public LogIngestionController(final LogIngestionService logIngestionService) {
        this.logIngestionService = logIngestionService;
    }

    /**
     * POST /logs/ingest - Batch Log Ingestion
     * <p>
     * Contract-First Design:
     * - Accepts a list of polymorphic log entries (ErrorLog, AuditLog, MetricLog)
     * - Returns 202 Accepted with a batchId for tracking
     * - Validates input using Jakarta Bean Validation
     * - Propagates TraceContext via ScopedValue for observability
     * <p>
     * OpenAPI Specification:
     * - Consumes: application/json
     * - Produces: application/json
     * - Status Codes:
     *   - 202 Accepted: Batch accepted for processing
     *   - 400 Bad Request: Invalid input (validation errors)
     *   - 422 Unprocessable Entity: Business logic validation failed
     *   - 429 Too Many Requests: Rate limit exceeded (via Resilience4j)
     *
     * @param logEntries List of log entries to ingest
     * @return LogIngestResponse with batchId and status
     */
    @PostMapping(
        value = "/ingest",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Batch Log Ingestion",
        description = """
            Accepts a batch of log entries for asynchronous processing.
            Supports polymorphic log types: ErrorLog, AuditLog, and MetricLog.
            Returns immediately with a tracking batchId.
            
            The 'type' field discriminates between log types:
            - ERROR: Error logs with exception details
            - AUDIT: Audit logs for compliance and security
            - METRIC: Performance metrics and observability data
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "Logs accepted for processing",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LogIngestResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input - validation errors"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unprocessable Entity - business logic validation failed"
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests - rate limit exceeded"
        )
    })
    public ResponseEntity<LogIngestResponse> ingestLogs(
        @Valid @RequestBody LogBatchRequest request
    ) {
        logger.info("Received log ingestion request: {}", request);

        // Generate batchId for tracking
        String batchId = UUID.randomUUID().toString();
        List<LogEntryRequest> logEntries = request.logs();

        // Extract or create TraceContext from the first entry
        TraceContext traceContext = extractTraceContext(logEntries);

        // Process logs within the trace context scope
        try {
            LogIngestResponse response = TraceContext.callWithContext(
                traceContext,
                () -> processLogEntries(batchId, logEntries, traceContext)
            );

            logger.info("Successfully processed log batch with ID: {}", batchId);
            // Return 202 Accepted for async processing
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            logger.error("Failed to process log entries", e);
            throw new RuntimeException("Failed to process log entries", e);
        }
    }

    /**
     * Extract TraceContext from the first log entry, or create a new one
     */
    private TraceContext extractTraceContext(List<LogEntryRequest> logEntries) {
        if (logEntries.isEmpty()) {
            return TraceContext.create();
        }

        var firstEntry = logEntries.get(0);
        var traceContextReq = firstEntry.traceContext();

        if (traceContextReq != null) {
            return new TraceContext(
                traceContextReq.traceId(),
                traceContextReq.spanId(),
                traceContextReq.correlationId(),
                traceContextReq.userId()
            );
        }

        return TraceContext.create();
    }

    /**
     * Process log entries and convert DTOs to domain models
     */
    private LogIngestResponse processLogEntries(
        String batchId,
        List<LogEntryRequest> logEntries,
        TraceContext traceContext
    ) throws Exception {

        List<LogEntry> domainLogs = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Convert DTOs to domain models
        for (int i = 0; i < logEntries.size(); i++) {
            try {
                LogEntry domainLog = convertToDomain(logEntries.get(i), traceContext);
                domainLogs.add(domainLog);
            } catch (Exception e) {
                errors.add("Entry %d: %s".formatted(i, e.getMessage()));
            }
        }

        // Delegate to service layer for business logic and persistence
        int totalAccepted = logIngestionService.ingestBatch(domainLogs);

        // Build response
        if (errors.isEmpty()) {
            return LogIngestResponse.success(batchId, totalAccepted);
        } else {
            return LogIngestResponse.partial(
                batchId,
                logEntries.size(),
                totalAccepted,
                errors.size(),
                errors
            );
        }
    }

    /**
     * Convert DTO to Domain Model (Pattern Matching with Sealed Interfaces)
     * <p>
     * Java 21 Pattern Matching ensures exhaustive handling of all subtypes.
     */
    private LogEntry convertToDomain(LogEntryRequest request, TraceContext traceContext) {
        Instant timestamp = request.timestamp() != null ? request.timestamp() : Instant.now();
        UUID id = UUID.randomUUID();

        return switch (request) {
            case ErrorLogRequest errorReq -> new ErrorLog(
                id,
                errorReq.message(),
                errorReq.source(),
                timestamp,
                traceContext,
                LogEntry.LogLevel.valueOf(errorReq.severity()),
                errorReq.exceptionType(),
                errorReq.stackTrace()
            );

            case AuditLogRequest auditReq -> new AuditLog(
                id,
                auditReq.message(),
                auditReq.source(),
                timestamp,
                traceContext,
                auditReq.userId(),
                auditReq.action(),
                auditReq.resourceType(),
                auditReq.resourceId(),
                auditReq.metadata()
            );

            case MetricLogRequest metricReq -> new MetricLog(
                id,
                metricReq.message(),
                metricReq.source(),
                timestamp,
                traceContext,
                metricReq.metricName(),
                metricReq.value(),
                metricReq.unit(),
                metricReq.tags()
            );
        };
    }
}
