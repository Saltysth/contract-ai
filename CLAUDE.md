# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Contract AI is a Java 17 Spring Boot 3.5.0 microservice that provides a unified AI chat interface supporting multiple AI models and service providers. The project uses a dual-module architecture with a strategy pattern for model routing and complete separation between core logic and external interfaces.

## Architecture

### Core Design Principles
- **Fixed Contract**: `chat(ChatRequest)` method signature as the stable external interface
- **Strategy Pattern**: Model-based routing to different AI providers using model name as the routing key
- **Service Discovery**: Nacos-based service discovery with Feign client integration
- **Default Configuration**: 60s timeout with 3 exponential backoff retries
- **Package Structure**: `com.contract.ai` with service name `contract-ai-service`

### Module Structure
1. **ai-feign-client**: External interface module
   - Defines DTOs, error codes, and ApiException
   - Provides AiClient.chat interface with fixed signature
   - Contains starter auto-configuration functionality

2. **ai-core**: Core service module
   - Implements strategy interfaces and routing logic
   - Contains ChatService with parameter validation and exception mapping
   - Provides REST ChatController with unified error handling
   - Includes placeholder strategies and configuration loader examples

## Development Commands

### Build and Test
```bash
# Compile project
mvn clean compile

# Run tests
mvn test

# Package project
mvn clean package

# Package skipping tests
mvn clean package -DskipTests
```

### Running the Application
```bash
# Run ai-core module
cd ai-core
mvn spring-boot:run

# Run packaged JAR
java -jar ai-core/target/contract-ai-service-1.0.0.jar
```

## Technology Stack

- **Runtime**: Java 17
- **Framework**: Spring Boot 3.5.0
- **Service Discovery**: Alibaba Nacos Discovery
- **HTTP Client**: Spring Cloud OpenFeign 2025.0.0
- **JSON Processing**: Jackson 2.18.0
- **Code Generation**: Lombok 1.18.36
- **Build Tool**: Maven multi-module

## Key Configuration

### Nacos Service Discovery
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        service: contract-ai-service
```

### Feign Client Configuration
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 60000
        readTimeout: 60000
        loggerLevel: basic
```

### Debug Logging
```yaml
logging:
  level:
    com.contract.ai: DEBUG
    org.springframework.cloud.openfeign: DEBUG
```

## Project Status

This is a design-stage project. Implementation work includes:
- External DTOs and exception definitions (ai-feign-client)
- Fixed-signature AiClient interface (ai-feign-client)
- Strategy interfaces and router implementation (ai-core)
- ChatService with validation and error mapping (ai-core)
- REST API controller with unified error handling (ai-core)
- Example strategies demonstrating model addition/switching (ai-core)

## Development Guidelines

### Adding New AI Models
1. Implement new strategy class in ai-core module
2. Add model configuration in application.yml
3. Register strategy with the router

### Code Style
- Use Lombok to reduce boilerplate code
- Follow Spring Boot best practices
- Use Jackson for JSON serialization/deserialization
- Unified exception handling using ApiException
- Semantic commit messages
- Ensure all tests pass before commits