package com.ghostlogger.core.domain.service;

import com.ghostlogger.core.domain.model.ErrorLog;
import com.ghostlogger.core.domain.model.LogEntry;
import com.ghostlogger.core.domain.model.TraceContext;
import com.ghostlogger.core.domain.port.AlertService;
import com.ghostlogger.core.domain.port.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * Structured Log Processor - Java 21 Structured Concurrency
 * <p>
 * Processes logs using StructuredTaskScope with fail-fast behavior.
 * When an ErrorLog is detected, triggers AlertService and StorageService in parallel.
 * <p>
 * Key Features:
 * - Uses Virtual Threads for lightweight concurrency
 * - Fail-Fast: If any subtask fails, entire scope fails immediately
 * - Structured Concurrency: Parent task waits for all children
 * - TraceContext propagation via ScopedValue
 * <p>
 * Architecture:
 * - AlertService: Sends alerts to external systems (PagerDuty, Slack)
 * - StorageService: Persists logs to long-term storage (S3, Elasticsearch)
 */
@Service
public class StructuredLogProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(StructuredLogProcessor.class);
    
    private final AlertService alertService;
    private final StorageService storageService;
    
    public StructuredLogProcessor(final AlertService alertService, final StorageService storageService) {
        this.alertService = alertService;
        this.storageService = storageService;
    }
    
    /**
     * Process a batch of log entries using Structured Concurrency
     * <p>
     * For each ErrorLog:
     * - Triggers AlertService and StorageService in parallel
     * - Uses StructuredTaskScope.ShutdownOnFailure for fail-fast behavior
     * - Propagates TraceContext to subtasks via ScopedValue
     * <p>
     * If any subtask fails, the entire scope shuts down immediately and the exception is propagated.
     * 
     * @param logEntries List of log entries to process
     * @return Number of successfully processed logs
     * @throws Exception if any subtask fails (fail-fast)
     */
    public int processBatch(final List<LogEntry> logEntries) throws Exception {
        if (logEntries == null || logEntries.isEmpty()) {
            log.warn("Received empty log batch for structured processing");
            return 0;
        }
        
        TraceContext currentContext = TraceContext.current();
        log.info("🔀 Processing {} logs with StructuredTaskScope [traceId={}]", 
            logEntries.size(), currentContext.traceId());
        
        int processed = 0;
        
        for (LogEntry entry : logEntries) {
            // Process ErrorLog with parallel alert + storage
            if (entry instanceof ErrorLog errorLog) {
                processErrorLogStructured(errorLog, currentContext);
                processed++;
            } else {
                // For non-error logs, just store
                storageService.store(entry);
                processed++;
            }
        }
        
        log.info("✅ Successfully processed {} logs with StructuredTaskScope [traceId={}]", 
            processed, currentContext.traceId());
        
        return processed;
    }
    
    /**
     * Process ErrorLog using StructuredTaskScope for parallel alert + storage
     * <p>
     * StructuredTaskScope.ShutdownOnFailure ensures:
     * - If alert fails → storage is cancelled
     * - If storage fails → alert is cancelled
     * - Parent waits for both to complete before continuing
     * <p>
     * This implements the "Fail-Fast" principle.
     */
    private void processErrorLogStructured(final ErrorLog errorLog, final TraceContext traceContext) throws Exception {
        log.debug("🔀 Processing ErrorLog with parallel tasks [id={}, traceId={}]", 
            errorLog.id(), traceContext.traceId());
        
        // StructuredTaskScope.ShutdownOnFailure: Fail-fast behavior
        // If any subtask fails, all other subtasks are cancelled
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            // Fork subtask 1: Send alert (with TraceContext propagation)
            Subtask<Void> alertTask = scope.fork(() -> {
                return TraceContext.callWithContext(traceContext, () -> {
                    log.debug("⚡ Alert task started [errorId={}, traceId={}]", 
                        errorLog.id(), traceContext.traceId());
                    alertService.sendAlert(errorLog);
                    log.debug("✅ Alert task completed [errorId={}, traceId={}]", 
                        errorLog.id(), traceContext.traceId());
                    return null;
                });
            });
            
            // Fork subtask 2: Store to long-term storage (with TraceContext propagation)
            Subtask<Void> storageTask = scope.fork(() -> {
                return TraceContext.callWithContext(traceContext, () -> {
                    log.debug("💾 Storage task started [errorId={}, traceId={}]", 
                        errorLog.id(), traceContext.traceId());
                    storageService.store(errorLog);
                    log.debug("✅ Storage task completed [errorId={}, traceId={}]", 
                        errorLog.id(), traceContext.traceId());
                    return null;
                });
            });
            
            // Join all subtasks - blocks until all complete or one fails
            // If any subtask fails, throwIfFailed() propagates the exception
            scope.join();
            scope.throwIfFailed();
            
            log.debug("✅ Parallel processing completed [errorId={}, traceId={}]", 
                errorLog.id(), traceContext.traceId());
            
        } catch (Exception e) {
            // Fail-fast: One of the subtasks failed, propagate the exception
            log.error("❌ Parallel processing failed [errorId={}, traceId={}]: {}", 
                errorLog.id(), traceContext.traceId(), e.getMessage());
            throw e;
        }
    }
}
