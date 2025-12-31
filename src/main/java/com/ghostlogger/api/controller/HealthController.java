package com.ghostlogger.api.controller;

import com.ghostlogger.api.dto.response.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Health Check Controller
 * <p>
 * Provides basic health status of the Ghost Logger service.
 * This is a simple example of REST API design following best practices.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health Check Endpoints")
public final class HealthController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Health Check",
        description = "Returns the current health status of the Ghost Logger service"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<HealthResponse> health() {
        var response = new HealthResponse(
            "UP",
            "Ghost Logger is running",
            Instant.now()
        );
        return ResponseEntity.ok(response);
    }
}
