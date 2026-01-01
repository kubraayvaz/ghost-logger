package com.ghostlogger.core.domain.service;

import com.ghostlogger.core.domain.model.ErrorLog;
import com.ghostlogger.core.domain.model.LogEntry;
import com.ghostlogger.core.domain.port.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Log Service - Domain Layer Business Logic
 * <p>
 * This service contains the core business logic for the Ghost Logger system.
 * It's framework-agnostic and depends only on domain models and ports.
 * <p>
 * Using constructor injection (final fields) for better testability - no @Autowired on fields.
 * <p>
 * Note: LogEntry is now a sealed interface with immutable record implementations.
 * Validation is handled in the record compact constructors.
 */
@Service
public class LogService {

    private static final Logger log = LoggerFactory.getLogger(LogService.class);
    private final LogRepository logRepository;

    public LogService(final LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Save a log entry
     * <p>
     * Business Logic:
     * - Logs critical error entries to application log
     * - Persists the entry via repository
     *
     * @param logEntry the log entry to save (immutable record)
     * @return the saved log entry
     */
    public LogEntry saveLogEntry(final LogEntry logEntry) {
        // Business logic: Log critical error entries
        if (logEntry instanceof ErrorLog errorLog) {
            if (errorLog.severity() == LogEntry.LogLevel.ERROR || 
                errorLog.severity() == LogEntry.LogLevel.FATAL) {
                log.warn("Critical error log entry received from source: {}", errorLog.source());
            }
        }

        return logRepository.save(logEntry);
    }

    /**
     * Retrieve a log entry by ID
     */
    public LogEntry getLogEntry(UUID id) {
        return logRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Log entry not found: " + id));
    }

    /**
     * Retrieve all log entries
     */
    public List<LogEntry> getAllLogEntries() {
        return logRepository.findAll();
    }

    /**
     * Retrieve log entries by source
     */
    public List<LogEntry> getLogEntriesBySource(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Source cannot be null or blank");
        }
        return logRepository.findBySource(source);
    }

    /**
     * Delete a log entry
     */
    public void deleteLogEntry(UUID id) {
        logRepository.deleteById(id);
    }
}
