# Observability Configuration

This document describes the observability features configured for the Order Management Service.

## Logging

The application uses Logback for logging with the following configuration:

- **Development Environment**: Human-readable logs with color coding
- **Production Environment**: JSON formatted logs for better parsing and analysis
- **Log Levels**: 
  - Application logs: DEBUG level
  - Spring framework logs: INFO level
  - MongoDB and Kafka logs: WARN level
- **Output**: Both console and file outputs
- **File Rotation**: Daily rotation with size-based splitting (100MB max per file)

## Distributed Tracing

The application uses Micrometer Tracing with Brave as the tracer implementation:

- **Sampling Rate**: 
  - Development: 100% of requests are traced
  - Production: 10% of requests are traced
- **Trace Context Propagation**: Automatic propagation through HTTP headers
- **Integration**: Works with Spring Web and other Spring components automatically

## Metrics

The application exposes metrics using Micrometer with Prometheus integration:

- **Endpoint**: `/actuator/prometheus` (on management port 8081)
- **Exposed Endpoints**: health, info, metrics, prometheus
- **Auto-configured Metrics**:
  - JVM metrics
  - HTTP server metrics
  - Kafka consumer/producer metrics
  - MongoDB metrics
  - Custom business metrics

## Health Checks

The application provides health check endpoints:

- **Application Health**: `/actuator/health` (on management port 8081)

## Ports

- **Application Port**: 8080 (main application endpoints)
- **Management Port**: 8081 (actuator endpoints including metrics and health)

## Environment-specific Configuration

- **Development**: `application-dev.yml`
- **Production**: `application-prod.yml`

## Testing Observability Features

1. **Logging**: Check console output or `logs/application.log` file
2. **Tracing**: Make HTTP requests and check trace IDs in logs
3. **Metrics**: Access `http://localhost:8081/actuator/prometheus`
4. **Health**: Access `http://localhost:8081/actuator/health`