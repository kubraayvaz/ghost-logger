package com.ghostlogger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ghost Logger - High-Performance Logging Service
 * <p>
 * Built with Hexagonal Architecture (Ports & Adapters):
 * - API Layer: Controllers, DTOs, Request/Response Validation
 * - Domain Layer: Business Logic, Domain Models, Ports (Interfaces)
 * - Infrastructure Layer: Database Adapters, External Service Clients
 * <p>
 * Features:
 * - Java 21 with Virtual Threads for I/O optimization
 * - Structured Concurrency (Preview Feature) for parallel operations
 * - Resilience4j for Circuit Breaking and Rate Limiting
 * - RESTful API Design (Richardson Maturity Level 3)
 * - OpenAPI 3.0 Documentation
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Ghost Logger API",
        version = "1.0.0",
        description = "High-Performance Logging Service with Hexagonal Architecture",
        contact = @Contact(
            name = "Ghost Logger Team",
            email = "team@ghostlogger.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development"),
        @Server(url = "https://api.ghostlogger.com", description = "Production")
    }
)
public class GhostLoggerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GhostLoggerApplication.class, args);
    }
}
