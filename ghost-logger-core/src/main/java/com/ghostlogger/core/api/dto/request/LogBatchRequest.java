package com.ghostlogger.core.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Wrapper for batch log ingestion requests
 */
public record LogBatchRequest(
    @NotEmpty(message = "Logs list cannot be empty")
    @Valid
    List<LogEntryRequest> logs
) {
}
