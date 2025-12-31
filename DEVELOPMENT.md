# 🛠️ Ghost Logger - Development Guide

**Contributor's Manual for building, testing, and extending Ghost Logger**

---

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Local Development Setup](#local-development-setup)
- [Building the Project](#building-the-project)
- [Running the Application](#running-the-application)
- [Testing Strategy](#testing-strategy)
- [Architecture & Design Decisions](#architecture--design-decisions)
- [Code Style & Standards](#code-style--standards)
- [Database Management](#database-management)
- [Performance Tuning](#performance-tuning)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## 🔧 Prerequisites

### Required Software

| Tool | Version | Installation Guide |
|------|---------|-------------------|
| **Java** | 21+ | [SDKMAN!](#installing-java-21-with-sdkman) |
| **Maven** | 3.9+ | [Apache Maven](https://maven.apache.org/install.html) |
| **Docker** | 20.10+ | [Docker Desktop](https://www.docker.com/products/docker-desktop) |
| **Git** | 2.30+ | [Git SCM](https://git-scm.com/downloads) |

### Installing Java 21 with SDKMAN!

**SDKMAN!** is the recommended way to manage Java versions:

```bash
# Install SDKMAN!
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 21 (choose one)
sdk install java 21.0.1-open       # OpenJDK
sdk install java 21.0.1-graalce    # GraalVM Community Edition

# Set default Java version
sdk default java 21.0.1-open

# Verify installation
java -version
```

**Expected Output:**
```
openjdk version "21.0.1" 2023-10-17
OpenJDK Runtime Environment (build 21.0.1+12-29)
OpenJDK 64-Bit Server VM (build 21.0.1+12-29, mixed mode, sharing)
```

---

## 🚀 Local Development Setup

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd ghost-logger
```

### 2. Build and Run

**Note**: The application currently uses **in-memory storage** (`ConcurrentHashMap`), so no database setup is required.

```bash
# Build the project
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run
```

### 3. (Optional) Start PostgreSQL for Future Development

PostgreSQL configuration exists but is not yet integrated:

```bash
# Start PostgreSQL and pgAdmin
docker compose up -d postgres pgadmin

# Verify services are running
docker ps
```

**Service Endpoints:**
- PostgreSQL: `localhost:5432`
- pgAdmin: [http://localhost:5050](http://localhost:5050)
  - Email: `admin@ghostlogger.com`
  - Password: `admin`

**TODO**: Implement JPA entities and repository layer to use PostgreSQL instead of in-memory storage.

---

## 🏗️ Building the Project

### Full Build with Tests

```bash
mvn clean install
```

### Skip Tests (Faster Feedback Loop)

```bash
mvn clean package -DskipTests
```

### Compile with Preview Features

Java 21 preview features (Structured Concurrency, ScopedValue) require `--enable-preview`:

```bash
mvn clean compile -Dcompilerargs="--enable-preview"
```

**This is already configured in `pom.xml`**, but verify if you encounter compilation errors.

### Build Native Image (GraalVM)

**Note**: GraalVM configuration exists in `pom.xml` but has not been tested.

**Experimental Build:**
```bash
mvn clean package -Pnative -DskipTests
./target/ghost-logger
```

**Known Issues**: Preview features (Structured Concurrency) may have limited GraalVM support.

---

## 🎮 Running the Application

### Run with Maven (Development)

```bash
mvn spring-boot:run
```

### Run Packaged JAR

```bash
java --enable-preview -jar target/ghost-logger-1.0.0-SNAPSHOT.jar
```

### Run in Debug Mode (IntelliJ IDEA)

1. Open `GhostLoggerApplication.java`
2. Right-click → **Debug 'GhostLoggerApplication.main()'**
3. Add VM options: `--enable-preview`
   - **Run > Edit Configurations > VM options**

### Run in Debug Mode (VS Code)

Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Ghost Logger",
      "request": "launch",
      "mainClass": "com.ghostlogger.GhostLoggerApplication",
      "projectName": "ghost-logger",
      "vmArgs": "--enable-preview"
    }
  ]
}
```

---

## 🧪 Testing Strategy

### Current Status

⚠️ **No tests currently implemented**. The following sections describe the recommended testing approach for contributors.

### Recommended Test Pyramid

```
        /\
       /  \      End-to-End Tests (TODO)
      /----\
     /      \    Integration Tests (TODO)
    /--------\
   /          \  Unit Tests (TODO)
  /____________\
```

### Unit Tests (TODO)

**Philosophy**: Test domain logic in isolation without Spring context.

```bash
# Run unit tests (when implemented)
mvn test
```

**Example Pattern: Testing Domain Service**
```java
@Test
void shouldIngestBatchWithRateLimiting() {
    // Arrange
    LogRepository mockRepo = mock(LogRepository.class);
    StructuredLogProcessor mockProcessor = mock(StructuredLogProcessor.class);
    LogIngestionService service = new LogIngestionService(mockRepo, mockProcessor);
    
    // Act
    int processed = service.ingestBatch(List.of(errorLog, auditLog));
    
    // Assert
    assertThat(processed).isEqualTo(2);
}
```

### Integration Tests (TODO)

**Philosophy**: Test with real database using Testcontainers.

**Prerequisites**: First implement PostgreSQL persistence layer.

```bash
# Run integration tests (when implemented)
mvn verify
```

### Performance Tests (TODO)

**Suggested Approach**: Use Apache Bench or JMeter after implementing persistence.

```bash
# Example load test command
ab -n 1000 -c 10 -T application/json -p sample-log.json \
   http://localhost:8080/api/v1/logs/ingest
```

---

## 🏛️ Architecture & Design Decisions

### Architectural Decision Records (ADRs)

#### ADR-001: Why Hexagonal Architecture?

**Status**: Accepted

**Context**: Traditional layered architecture tightly couples domain logic to frameworks (Spring, JPA).

**Decision**: Adopt Hexagonal Architecture (Ports & Adapters) to:
1. **Domain Independence**: Core business logic is framework-agnostic
2. **Testability**: Mock adapters without Spring Boot Test overhead
3. **Flexibility**: Swap PostgreSQL for Cassandra without domain changes

**Consequences**:
- ✅ **Pro**: Easy to test domain services with pure Java
- ✅ **Pro**: Can migrate from Spring to Quarkus with minimal effort
- ❌ **Con**: More boilerplate (interfaces for every adapter)

---

#### ADR-002: Why Java 21 Virtual Threads?

**Status**: Accepted

**Context**: Traditional thread-per-request model limits scalability to ~5,000 concurrent connections (bounded by thread pool size).

**Decision**: Enable Virtual Threads (`spring.threads.virtual.enabled=true`) to:
1. Demonstrate modern Java 21 concurrency capabilities
2. Simplify async code (no CompletableFuture chains)
3. Reduce memory overhead (1KB per virtual thread vs. 1MB per platform thread)

**Consequences**:
- ✅ **Pro**: Potential for high concurrency in I/O-bound workloads
- ✅ **Pro**: Simpler code (blocking I/O works efficiently)
- ❌ **Con**: Requires Java 21+ (not compatible with older JVMs)
- ⚠️ **Note**: Performance benefits not yet benchmarked in this project

---

#### ADR-003: Why Sealed Interfaces for Log Types?

**Status**: Accepted

**Context**: Polymorphic log types (ErrorLog, AuditLog, MetricLog) were modeled as inheritance hierarchy.

**Decision**: Use `sealed interface LogEntry` to:
1. **Exhaustive Pattern Matching**: Compiler enforces handling all log types
2. **Type Safety**: Prevents external classes from implementing `LogEntry`
3. **Immutability**: Records guarantee thread-safe log entries

**Example:**
```java
sealed interface LogEntry permits ErrorLog, AuditLog, MetricLog {}

record ErrorLog(String message, String severity) implements LogEntry {}
record AuditLog(String userId, String action) implements LogEntry {}
record MetricLog(String name, double value) implements LogEntry {}
```

**Pattern Matching:**
```java
String processLog(LogEntry log) {
    return switch (log) {
        case ErrorLog e -> alertService.sendAlert(e);
        case AuditLog a -> auditService.store(a);
        case MetricLog m -> metricService.record(m);
        // Compiler error if we forget a case!
    };
}
```

---

#### ADR-004: Why Resilience4j Over Spring Retry?

**Status**: Accepted

**Context**: Need circuit breaker, rate limiter, and retry patterns.

**Decision**: Use Resilience4j because:
1. **Feature-Rich**: Circuit breaker, bulkhead, rate limiter, retry in one library
2. **Annotation-Driven**: `@RateLimiter`, `@CircuitBreaker` work with Spring AOP
3. **Observability**: Built-in Actuator integration for health checks

**Configuration:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      ghostLoggerService:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
  ratelimiter:
    instances:
      ghostLoggerApi:
        limit-for-period: 100
        limit-refresh-period: 1s
```

---

### Domain Model Design

#### Sealed Interface Hierarchy

```
         LogEntry (sealed interface)
              |
    +---------+---------+
    |         |         |
ErrorLog  AuditLog  MetricLog
(record)  (record)  (record)
```

#### Ports (Interfaces)

```java
// Domain defines contract, infrastructure implements
public interface LogRepository {
    void save(LogEntry log);
    List<LogEntry> findByTraceId(String traceId);
}

// Infrastructure provides implementation
@Repository
public class PostgresLogRepository implements LogRepository {
    // JPA/JDBC implementation
}
```

---

## 📐 Code Style & Standards

### Java Coding Standards

Follow **Google Java Style Guide** with these additions:

#### Naming Conventions
- **Classes**: `PascalCase` (e.g., `LogIngestionService`)
- **Methods**: `camelCase` (e.g., `ingestBatch()`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_BATCH_SIZE`)
- **Packages**: `lowercase` (e.g., `com.ghostlogger.domain.service`)

#### Record Usage
**Prefer records for immutable DTOs:**
```java
// ✅ Good
public record LogIngestResponse(String batchId, String status, int count) {}

// ❌ Bad (unnecessary boilerplate)
public class LogIngestResponse {
    private final String batchId;
    // ... getters, equals, hashCode, toString
}
```

#### Constructor Injection
**Always use constructor injection (not @Autowired fields):**
```java
// ✅ Good
@Service
public class LogService {
    private final LogRepository repository;
    
    public LogService(LogRepository repository) {
        this.repository = repository;
    }
}

// ❌ Bad
@Service
public class LogService {
    @Autowired
    private LogRepository repository;  // Hard to test
}
```

### Git Workflow (Git Flow)

#### Branch Naming
- `main` - Production-ready code
- `develop` - Integration branch for features
- `feat/feature-name` - Feature branches
- `fix/bug-description` - Bug fixes
- `hotfix/critical-issue` - Production hotfixes

#### Commit Messages (Conventional Commits)

**Format**: `<type>(<scope>): <subject>`

**Types**:
- `feat`: New feature (e.g., `feat(api): add batch ingestion endpoint`)
- `fix`: Bug fix (e.g., `fix(db): resolve connection pool leak`)
- `docs`: Documentation (e.g., `docs(readme): update quick start`)
- `refactor`: Code restructuring (e.g., `refactor(service): extract helper method`)
- `test`: Add/update tests (e.g., `test(integration): add Testcontainers setup`)
- `chore`: Build/tooling (e.g., `chore(maven): upgrade spring boot to 3.2.1`)

**Examples**:
```bash
git commit -m "feat(resilience): add circuit breaker to alert service"
git commit -m "fix(controller): handle null trace context"
git commit -m "docs(adr): document decision to use virtual threads"
```

---

## 🗄️ Database Management

### Current Implementation

The project currently uses **in-memory storage** via `InMemoryLogRepository`:

```java
@Repository
public class InMemoryLogRepository implements LogRepository {
    private final Map<UUID, LogEntry> storage = new ConcurrentHashMap<>();
    // Thread-safe operations for demo purposes
}
```

**Limitations**:
- ❌ Data lost on application restart
- ❌ No query capabilities (no SQL)
- ❌ Not suitable for production use

### Future: PostgreSQL Integration (TODO)

#### Planned Approach: Flyway Migrations

**Step 1**: Add Flyway dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

**Step 2**: Create migration in `src/main/resources/db/migration/`:

**V1__initial_schema.sql:**
```sql
CREATE TABLE log_entries (
    id UUID PRIMARY KEY,
    log_type VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20),
    source VARCHAR(255),
    trace_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trace_id ON log_entries(trace_id);
```

**Step 3**: Implement JPA repository to replace in-memory version.

### Local Database Access (If PostgreSQL Running)

#### Connect with psql
```bash
docker exec -it ghost-logger-db psql -U ghostlogger -d ghostlogger
```

#### Reset Database
```bash
docker compose down -v  # Delete volumes
docker compose up -d postgres
```

---

## ⚡ Performance Tuning

### JVM Tuning

**Production JVM Flags:**
```bash
java --enable-preview \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -Xms512m \
     -Xmx2g \
     -XX:+HeapDumpOnOutOfMemoryError \
     -jar ghost-logger.jar
```

### Connection Pool Tuning (Future)

**Note**: Connection pooling is configured in `application.yml` but not currently used (in-memory storage).

When PostgreSQL is integrated, adjust HikariCP settings:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20          # Match DB max connections
      minimum-idle: 5
      connection-timeout: 30000      # 30 seconds
```

**Formula**: `max-pool-size = (core_count * 2) + disk_count`

### Rate Limiting

**Current Configuration**: `@RateLimiter` annotation exists in code.

**Adjust in `application.yml`:**
```yaml
resilience4j:
  ratelimiter:
    instances:
      logIngestion:
        limit-for-period: 100        # Current setting
        limit-refresh-period: 1s
```

### Batch Size Optimization (Future)

**TODO**: Configure Hibernate batch inserts when JPA persistence is implemented:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
```

---

## 🚢 Deployment

### Containerization (Docker)

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ghost-logger-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
```

**Build and run:**
```bash
docker build -t ghost-logger:latest .
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  ghost-logger:latest
```

### Kubernetes Deployment

**deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ghost-logger
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ghost-logger
  template:
    metadata:
      labels:
        app: ghost-logger
    spec:
      containers:
      - name: ghost-logger
        image: ghost-logger:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          value: "postgres-service"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
```

---

## 🐛 Troubleshooting

### Common Issues

#### 1. `ClassNotFoundException: jdk.incubator.concurrent.StructuredTaskScope`

**Cause**: Preview features not enabled.

**Solution**:
```bash
# Add --enable-preview to JVM args
java --enable-preview -jar target/ghost-logger-1.0.0-SNAPSHOT.jar
```

#### 2. `Connection refused: localhost:5432`

**Cause**: PostgreSQL container not running.

**Solution**:
```bash
docker compose up -d postgres
docker ps  # Verify container is running
```

#### 3. `Lombok annotations not working`

**Cause**: IDE annotation processing not enabled.

**Solution (IntelliJ IDEA)**:
1. Settings → Build, Execution, Deployment → Compiler → Annotation Processors
2. Enable "Enable annotation processing"

#### 4. Rate Limiter Triggers Too Often

**Cause**: Default limit is 100 requests/second.

**Solution**: Increase limit in `application.yml`:
```yaml
resilience4j:
  ratelimiter:
    instances:
      ghostLoggerApi:
        limit-for-period: 500
```

---

## 🤝 Contributing

### Pull Request Process

1. **Fork and clone the repository**
   ```bash
   git clone https://github.com/your-username/ghost-logger.git
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feat/your-feature-name
   ```

3. **Make changes and test**
   ```bash
   mvn clean verify  # Run all tests
   ```

4. **Commit with Conventional Commits**
   ```bash
   git commit -m "feat(service): add Kafka streaming adapter"
   ```

5. **Push and create PR**
   ```bash
   git push origin feat/your-feature-name
   ```

6. **Code Review Checklist**
   - [ ] Tests pass (`mvn verify`)
   - [ ] Code follows style guide
   - [ ] Documentation updated (if needed)
   - [ ] No secrets in code
   - [ ] Conventional commit message

### Code Review Standards

**Required Approvals**: 1 maintainer

**Review Criteria**:
- **Correctness**: Does the code solve the problem?
- **Tests**: Unit + integration tests included?
- **Performance**: No obvious bottlenecks?
- **Documentation**: Public APIs documented?

---

## 📚 Additional Resources

- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/docs/3.2.1/reference/html/
- **Java 21 Features**: https://openjdk.org/projects/jdk/21/
- **Resilience4j Guide**: https://resilience4j.readme.io/docs
- **Hexagonal Architecture**: https://alistair.cockburn.us/hexagonal-architecture/
- **Testcontainers**: https://www.testcontainers.org/

---

**Happy Coding! 🚀**

This is an educational project demonstrating Java 21 features and Hexagonal Architecture patterns.
