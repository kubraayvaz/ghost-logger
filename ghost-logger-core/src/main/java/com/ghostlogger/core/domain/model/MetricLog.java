package com.ghostlogger.core.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Metric Log Record
 * <p>
 * Captures performance metrics, KPIs, and observability data.
 * Includes metric name, value, unit, and optional tags.
 */
public record MetricLog(
    UUID id,
    String message,
    String source,
    Instant timestamp,
    TraceContext traceContext,
    String metricName,
    double value,
    String unit,
    Map<String, String> tags
) implements LogEntry {
    
    public MetricLog {
        if (metricName == null || metricName.isBlank()) {
            throw new IllegalArgumentException("MetricName cannot be null or blank");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Unit cannot be null or blank");
        }
    }
}
