package com.ghostlogger.core.domain.model;

import java.util.UUID;

/**
 * Trace Context Record (Java 21+)
 * <p>
 * Represents distributed tracing context with correlationId, spanId, and traceId.
 * This is used with ScopedValue for thread-safe context propagation.
 * <p>
 * ScopedValue provides a safe and efficient way to share immutable data within a thread
 * and its child threads, especially with Virtual Threads.
 */
public record TraceContext(
    String traceId,
    String spanId,
    String correlationId,
    String userId
) {
    
    /**
     * ScopedValue for thread-safe context propagation (Java 21+ Preview Feature)
     * <p>
     * ScopedValue is preferred over ThreadLocal for Virtual Threads because:
     * - Immutable and safer
     * - Better memory management
     * - Explicit scope boundaries
     * - No memory leaks from forgotten cleanup
     * <p>
     * Note: Requires --enable-preview flag in compiler args
     */
    public static final ScopedValue<TraceContext> SCOPED_TRACE_CONTEXT = ScopedValue.newInstance();
    
    /**
     * Compact constructor for validation
     */
    public TraceContext {
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        if (spanId == null || spanId.isBlank()) {
            spanId = UUID.randomUUID().toString();
        }
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = traceId;
        }
    }
    
    /**
     * Factory method to create a new TraceContext
     */
    public static TraceContext create() {
        return new TraceContext(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            null
        );
    }
    
    /**
     * Factory method with userId
     */
    public static TraceContext create(String userId) {
        return new TraceContext(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            userId
        );
    }
    
    /**
     * Get current trace context from ScopedValue
     * <p>
     * Returns the TraceContext bound to the current scope, or creates a new one if not present.
     */
    public static TraceContext current() {
        return SCOPED_TRACE_CONTEXT.isBound() 
            ? SCOPED_TRACE_CONTEXT.get() 
            : create();
    }
    
    /**
     * Execute code within a trace context scope
     * <p>
     * Example usage:
     * <pre>
     * TraceContext.runWithContext(traceContext, () -> {
     *     // Code here has access to TraceContext.current()
     *     logService.processLogs(entries);
     * });
     * </pre>
     */
    public static void runWithContext(TraceContext context, Runnable task) {
        ScopedValue.where(SCOPED_TRACE_CONTEXT, context).run(task);
    }
    
    /**
     * Call code within a trace context scope and return a result
     */
    public static <T> T callWithContext(TraceContext context, java.util.concurrent.Callable<T> task) throws Exception {
        return ScopedValue.where(SCOPED_TRACE_CONTEXT, context).call(task);
    }
}
