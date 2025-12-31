package com.ghostlogger.infrastructure.adapter;

import com.ghostlogger.domain.model.LogEntry;
import com.ghostlogger.domain.port.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Default Storage Service Implementation
 * <p>
 * Simulates storing logs to long-term storage (S3, Elasticsearch, etc.)
 */
@Service
public class DefaultStorageService implements StorageService {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultStorageService.class);
    
    @Override
    public void store(LogEntry logEntry) throws Exception {
        // Simulate I/O latency
        Thread.sleep(150);
        
        log.debug("💾 STORED: {} [id={}, traceId={}]",
            logEntry.message(),
            logEntry.id(),
            logEntry.traceContext().traceId());
        
        // Simulate potential storage failure (5% failure rate for testing)
        if (Math.random() < 0.05) {
            throw new Exception("Storage service temporarily unavailable");
        }
    }
}
