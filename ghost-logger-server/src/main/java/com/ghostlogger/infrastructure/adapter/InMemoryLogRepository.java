package com.ghostlogger.infrastructure.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.ghostlogger.core.domain.model.LogEntry;
import com.ghostlogger.core.domain.port.LogRepository;

/**
 * In-Memory Log Repository Adapter
 * <p>
 * This is an ADAPTER in Hexagonal Architecture - implements the LogRepository port.
 * Uses ConcurrentHashMap for thread-safe operations with Virtual Threads.
 * <p>
 * Note: This is a simple in-memory implementation for demonstration.
 * In production, replace with a JPA/JDBC implementation backed by PostgreSQL.
 */
@Repository
public class InMemoryLogRepository implements LogRepository {

    private final Map<UUID, LogEntry> storage = new ConcurrentHashMap<>();

    @Override
    public LogEntry save(LogEntry logEntry) {
        storage.put(logEntry.id(), logEntry);
        return logEntry;
    }

    @Override
    public Optional<LogEntry> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<LogEntry> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<LogEntry> findBySource(String source) {
        return storage.values().stream()
            .filter(entry -> source.equals(entry.source()))
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        storage.remove(id);
    }
}
