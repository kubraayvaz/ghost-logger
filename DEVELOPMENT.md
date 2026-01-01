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
- [Contributing](#contributing)

---

## 🔧 Prerequisites

### Required Software

| Tool       | Version | Installation Guide |
|------------|---------|-------------------|
| **Java**   | 21+     | [SDKMAN!](#installing-java-21-with-sdkman) |
| **Maven**  | 3.9+    | [Apache Maven](https://maven.apache.org/install.html) |
| **Docker** | 20.10+  | [Docker Desktop](https://www.docker.com/products/docker-desktop) |
| **Git**    | 2.30+   | [Git SCM](https://git-scm.com/downloads) |

### Installing Java 21 with SDKMAN!

```bash
# Install SDKMAN!
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 21
sdk install java 21.0.1-open
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

```bash
# Build the project
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run
```

### 3. (Optional) Start PostgreSQL

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

---

## 🏗️ Building the Project

```bash
# Full build with tests
mvn clean install

# Skip tests for faster feedback
mvn clean package -DskipTests
```

---

## 🎮 Running the Application

```bash
# Run with Maven
mvn spring-boot:run

# Run packaged JAR
java --enable-preview -jar target/ghost-logger-1.0.0-SNAPSHOT.jar
```

---

## 🧪 Testing Strategy

### Current Status

⚠️ **Tests are partially implemented.**

### Running Tests

```bash
mvn test  # Run unit tests
mvn verify  # Run integration tests
```

---

## 🏛️ Architecture & Design Decisions

### Key Decisions

1. **Hexagonal Architecture**: Ensures domain independence and testability.
2. **Java 21 Virtual Threads**: Enables high concurrency with low memory overhead.
3. **Sealed Interfaces**: Provides exhaustive pattern matching for log types.
4. **Resilience4j**: Combines circuit breaker, rate limiter, and retry patterns.

---

## 📐 Code Style & Standards

- Follow **Google Java Style Guide**.
- Use **records** for immutable DTOs.
- Prefer **constructor injection** over `@Autowired` fields.

---

## 🗄️ Database Management

### Current State

- **In-Memory Storage**: Used for development and testing.
- **PostgreSQL Integration**: Planned with Flyway migrations.

---

## ⚡ Performance Tuning

### JVM Tuning

```bash
java --enable-preview \
     -XX:+UseG1GC \
     -Xms512m \
     -Xmx2g \
     -XX:+HeapDumpOnOutOfMemoryError \
     -jar ghost-logger.jar
```

---

## 🤝 Contributing

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
   mvn clean verify
   ```

4. **Commit with Conventional Commits**
   ```bash
   git commit -m "feat(service): add Kafka streaming adapter"
   ```

5. **Push and create PR**
   ```bash
   git push origin feat/your-feature-name
   ```

---

## 📚 Additional Resources

- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/docs/3.2.1/reference/html/
- **Java 21 Features**: https://openjdk.org/projects/jdk/21/
- **Resilience4j Guide**: https://resilience4j.readme.io/docs
- **Hexagonal Architecture**: https://alistair.cockburn.us/hexagonal-architecture/
- **Testcontainers**: https://www.testcontainers.org/
