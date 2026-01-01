package com.ghostlogger.core.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Health Response DTO
 * <p>
 * Using Java 21 Records for immutable DTOs - best practice for API responses.
 * Records automatically generate:
 * - Constructor
 * - Getters
 * - equals(), hashCode(), toString()
 */
@Schema(description = "Health status response")
public record HealthResponse(
    @Schema(description = "Service status", example = "UP")
    String status,
    
    @Schema(description = "Status message", example = "Ghost Logger is running")
    String message,
    
    @Schema(description = "Timestamp of the health check")
    Instant timestamp
) {}
