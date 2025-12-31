package com.ghostlogger.client;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogbackHttpAppender extends AppenderBase<ILoggingEvent> {
    private final BlockingQueue<ILoggingEvent> queue = new LinkedBlockingQueue<>(1000);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private String serverUrl;
    private HttpClient httpClient;
    private Thread worker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public void start() {
        System.out.println("[LogbackHttpAppender] Initializing appender. serverUrl=" + serverUrl);
        if (serverUrl == null) throw new IllegalStateException("serverUrl must be set");
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        running.set(true);
        worker = Thread.ofVirtual().start(this::processQueue);
        super.start();
    }

    @Override
    public void stop() {
        running.set(false);
        if (worker != null) worker.interrupt();
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!queue.offer(event)) {
            // Optionally drop or handle overflow
        }
    }

    private void processQueue() {
        List<ILoggingEvent> batch = new ArrayList<>(20);
        while (running.get()) {
            try {
                ILoggingEvent first = queue.poll(1, TimeUnit.SECONDS);
                if (first != null) {
                    batch.add(first);
                    queue.drainTo(batch, 19);
                    sendBatch(new ArrayList<>(batch));
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void sendBatch(List<ILoggingEvent> events) {
        if (events.isEmpty()) return;
        List<Map<String, Object>> logs = new ArrayList<>();
        for (ILoggingEvent e : events) {
            Map<String, Object> log = new HashMap<>();
            log.put("type", "ERROR"); // or map from e.getLevel()
            log.put("message", e.getFormattedMessage());
            log.put("source", e.getLoggerName());
            log.put("severity", e.getLevel().toString());
            log.put("exceptionType", e.getThrowableProxy() != null ? e.getThrowableProxy().getClassName() : null);
            log.put("stackTrace", e.getThrowableProxy() != null ? Arrays.toString(e.getThrowableProxy().getStackTraceElementProxyArray()) : null);
            logs.add(log);
        }
        Map<String, Object> payload = Map.of("logs", logs);
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            System.out.println("[LogbackHttpAppender] Failed to serialize logs: " + ex.getMessage());
            return;
        }

        System.out.println("[LogbackHttpAppender] Sending batch of " + logs.size() + " logs to " + serverUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(5))
                .build();

        int attempts = 0;
        while (attempts < 3) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("[LogbackHttpAppender] Server response: " + response.statusCode() + " - " + response.body());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("[LogbackHttpAppender] HTTP send failed: " + e.getMessage());
                try { Thread.sleep(500L * (attempts + 1)); } catch (InterruptedException ignored) {}
            }
            attempts++;
        }
    }
}
