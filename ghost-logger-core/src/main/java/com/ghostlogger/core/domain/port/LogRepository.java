package com.ghostlogger.core.domain.port;

import com.ghostlogger.core.domain.model.LogEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Log Repository Port (Interface)
 * <p>
 * This is a PORT in Hexagonal Architecture - defines the contract
 * for persistence operations without coupling to a specific implementation.
 * <p>
 * The Infrastructure layer will provide the ADAPTER implementation.
 */
public interface LogRepository {

    /**
     * Save a log entry
     * @param logEntry the log entry to save
     * @return the saved log entry with generated ID
     */
    LogEntry save(LogEntry logEntry);

    /**
     * Find a log entry by ID
     * @param id the log entry ID
     * @return Optional containing the log entry if found
     */
    Optional<LogEntry> findById(UUID id);

    /**
     * Find all log entries
     * @return list of all log entries
     */
    List<LogEntry> findAll();

    /**
     * Find log entries by source
     * @param source the source identifier
     * @return list of log entries from the specified source
     */
    List<LogEntry> findBySource(String source);

    /**
     * Delete a log entry by ID
     * @param id the log entry ID
     */
    void deleteById(UUID id);
}
