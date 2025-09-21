# Order Management Service

This is the Order Management Service for Paklog, implementing the capabilities defined in the OpenAPI and AsyncAPI specifications.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Event Publishing](#event-publishing)
- [Getting Started](#getting-started)
- [Testing](#testing)

## Overview

The Order Management Service handles the lifecycle of fulfillment orders, including creation, retrieval, and cancellation. It also publishes domain events to enable decoupled, choreographed workflows with other bounded contexts.

## Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.3.3
- **Database**: MongoDB
- **Messaging**: Apache Kafka
- **Event Standard**: CloudEvents
- **Build Tool**: Maven

## Architecture

This service follows the Hexagonal Architecture (Ports and Adapters) pattern:

- **Domain Layer**: Contains the business logic and domain models
- **Application Layer**: Contains the application services that orchestrate the business logic
- **Infrastructure Layer**: Contains the implementations of the ports (repositories, messaging, etc.)
- **Interfaces Layer**: Contains the REST controllers and DTOs

## API Endpoints

### Create Fulfillment Order

```
POST /fulfillment_orders
```

Creates a new fulfillment order. Returns a 202 Accepted status on success or 409 Conflict if an order with the same sellerFulfillmentOrderId already exists.

### Get Fulfillment Order by ID

```
GET /fulfillment_orders/{order_id}
```

Retrieves a fulfillment order by its system-generated ID. Returns 200 OK with the order details or 404 Not Found if the ID does not exist.

### Cancel Fulfillment Order

```
POST /fulfillment_orders/{order_id}/cancel
```

Cancels a fulfillment order. Returns 202 Accepted on success or 400 Bad Request if the cancellation is not allowed by the order's state.

## Event Publishing

The service publishes the following domain events to a Kafka topic in CloudEvents format:

- `com.example.fulfillment.order.received`: Published when a new fulfillment order is successfully accepted
- `com.example.fulfillment.order.validated`: Published when an order has passed all business rule validations
- `com.example.fulfillment.order.invalidated`: Published when an order fails business rule validation
- `com.example.fulfillment.order.cancelled`: Published when an order has been successfully cancelled

Events are published using the transactional outbox pattern to ensure at-least-once delivery.

## Getting Started

### Prerequisites

- Java 21
- Maven 3.6+
- Docker and Docker Compose (for containerized deployment)
- MongoDB (if running without Docker)
- Apache Kafka (if running without Docker)

### Building the Application

```bash
mvn clean install
```

### Running the Application with Docker Compose (Recommended)

The easiest way to run the application is with Docker Compose, which will start the application along with all its dependencies:

```bash
docker-compose up --build
```

This will start:
- The Order Management Service on port 8080
- MongoDB on port 27017
- Kafka on port 9092

For development, you can use the override configuration which provides live reloading:

```bash
docker-compose up
```

### Running in Production

For production deployment, use the production docker-compose file:

```bash
# Copy the example environment file and modify it with your secure passwords
cp .env.example .env
# Edit .env file to set secure passwords

# Run with production configuration
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Running the Application Locally

If you prefer to run the application locally without Docker, you'll need to have MongoDB and Kafka running separately:

```bash
mvn spring-boot:run
```

Or:

```bash
java -jar target/order-management-0.0.1-SNAPSHOT.jar
```

### Configuration

The application can be configured using the `application.yml` file in `src/main/resources`.

## Testing

### Unit Tests

Run unit tests with:

```bash
mvn test
```

### Integration Tests

Run integration tests with:

```bash
mvn verify
```

## CI/CD

This project includes GitHub Actions workflows for continuous integration and continuous deployment:

- **CI Pipeline**: Automatically builds and tests the application on every push or pull request
- **CD Staging**: Deploys to staging environment when changes are pushed to the `develop` branch
- **CD Production**: Deploys to production environment when a new release is published
- **Security Scanning**: Regular security scans using OWASP Dependency Check and SpotBugs

For more details about the workflows, see [.github/README.md](.github/README.md).

## Observability

This service includes comprehensive observability features:

### Logging

The application uses Logback for logging with structured JSON output in production environments.
Log files are written to the `logs/` directory with daily rotation.

### Distributed Tracing

Distributed tracing is implemented using Micrometer Tracing with Brave.
Trace context is automatically propagated through HTTP requests.

### Metrics

Application metrics are exposed in Prometheus format at:
```
http://localhost:8081/actuator/prometheus
```

### Health Checks

Health check endpoints are available at:
```
http://localhost:8081/actuator/health
```

For detailed configuration, see [observability-README.md](src/main/resources/observability-README.md).
