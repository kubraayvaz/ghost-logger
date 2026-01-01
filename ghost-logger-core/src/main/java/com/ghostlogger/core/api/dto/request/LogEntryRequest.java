package com.ghostlogger.core.api.dto.request;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotBlank;

/**
 * Log Entry Request DTO (Sealed Interface Pattern)
 * <p>
 * Contract-First API Design: This DTO defines the API contract.
 * Jackson polymorphic deserialization is configured to map JSON to the correct subtype.
 * <p>
 * Using @JsonTypeInfo for runtime type discrimination during deserialization.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ErrorLogRequest.class, name = "ERROR"),
    @JsonSubTypes.Type(value = AuditLogRequest.class, name = "AUDIT"),
    @JsonSubTypes.Type(value = MetricLogRequest.class, name = "METRIC")
})
public sealed interface LogEntryRequest permits ErrorLogRequest, AuditLogRequest, MetricLogRequest {
    
    String type();
    
    @NotBlank(message = "Message cannot be blank")
    String message();
    
    @NotBlank(message = "Source cannot be blank")
    String source();
    
    Instant timestamp();
    
    TraceContextRequest traceContext();
}
