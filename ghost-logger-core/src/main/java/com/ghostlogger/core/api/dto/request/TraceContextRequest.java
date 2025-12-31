package com.ghostlogger.api.dto.request;

/**
 * Trace Context Request DTO
 */
public record TraceContextRequest(
    String traceId,
    String spanId,
    String correlationId,
    String userId
) {
}
