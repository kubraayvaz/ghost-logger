package com.ghostlogger.core.domain.port;

import com.ghostlogger.core.domain.model.LogEntry;

/**
 * Storage Service Port - Hexagonal Architecture
 * <p>
 * Persists logs to long-term storage (e.g., S3, Elasticsearch)
 */
public interface StorageService {
    
    /**
     * Store log entry to long-term storage
     * 
     * @param logEntry The log entry to store
     * @throws Exception if storage fails
     */
    void store(LogEntry logEntry) throws Exception;
}
