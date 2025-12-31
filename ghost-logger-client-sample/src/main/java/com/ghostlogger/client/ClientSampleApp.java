package com.ghostlogger.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientSampleApp {

    private static final Logger logger = LoggerFactory.getLogger(ClientSampleApp.class);

    public static void main(String[] args) throws InterruptedException {
        // Create a fixed thread pool for asynchronous logging
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 10; i++) {
            int logIndex = i;
            executorService.submit(() -> {
                logger.info("Log message number: {}", logIndex);
                try {
                    sendLogToServer("Log message number: " + logIndex);
                } catch (Exception e) {
                    logger.error("Failed to send log to server", e);
                }
            });
        }

        // Shutdown the executor service gracefully
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        logger.info("All log messages have been sent.");
    }

    private static void sendLogToServer(String logMessage) throws Exception {
        URL url = new URL("http://localhost:8080/api/v1/logs/ingest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        // Updated payload with required fields
        String jsonPayload = "{\"logs\":[{" +
            "\"type\":\"ERROR\"," +
            "\"message\":\"" + logMessage + "\"," +
            "\"source\":\"ClientSampleApp\"," +
            "\"timestamp\":\"" + java.time.Instant.now() + "\"," +
            "\"severity\":\"ERROR\"}]}";

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonPayload.getBytes());
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_ACCEPTED) {
            // Log the response body for debugging
            try (var is = connection.getErrorStream()) {
                String errorResponse = new String(is.readAllBytes());
                throw new RuntimeException("Failed to send log: HTTP error code " + responseCode + ", Response: " + errorResponse);
            }
        }
    }
}