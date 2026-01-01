package com.ghostlogger.core.api.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Log Ingest Response DTO
 * <p>
 * Contract-First API Design: This defines the response structure.
 * Returns 202 Accepted for async processing with a tracking batchId.
 */
public record LogIngestResponse(
    String batchId,
    int totalReceived,
    int totalAccepted,
    int totalRejected,
    List<String> errors,
    Instant receivedAt,
    String status
) {
    
    /**
     * Factory method for successful ingestion
     */
    public static LogIngestResponse success(String batchId, int totalAccepted) {
        return new LogIngestResponse(
            batchId,
            totalAccepted,
            totalAccepted,
            0,
            List.of(),
            Instant.now(),
            "ACCEPTED"
        );
    }
    
    /**
     * Factory method for partial success
     */
    public static LogIngestResponse partial(
        String batchId, 
        int totalReceived, 
        int totalAccepted, 
        int totalRejected,
        List<String> errors
    ) {
        return new LogIngestResponse(
            batchId,
            totalReceived,
            totalAccepted,
            totalRejected,
            errors,
            Instant.now(),
            "PARTIAL"
        );
    }
}
