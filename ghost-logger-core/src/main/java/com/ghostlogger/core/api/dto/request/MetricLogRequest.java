package com.ghostlogger.core.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * Metric Log Request DTO
 */
public record MetricLogRequest(
    String type,
    
    @NotBlank(message = "Message cannot be blank")
    String message,
    
    @NotBlank(message = "Source cannot be blank")
    String source,
    
    Instant timestamp,
    
    TraceContextRequest traceContext,
    
    @NotBlank(message = "MetricName cannot be blank")
    String metricName,
    
    @NotNull(message = "Value cannot be null")
    Double value,
    
    @NotBlank(message = "Unit cannot be blank")
    String unit,
    
    Map<String, String> tags
) implements LogEntryRequest {
    
    public MetricLogRequest {
        if (type == null) {
            type = "METRIC";
        }
    }
}
