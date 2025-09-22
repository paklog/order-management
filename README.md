# Order Management Service

This is the Order Management Service for Paklog, a key component in our logistics and fulfillment platform. It is responsible for managing the entire lifecycle of a fulfillment order, from its creation to its final shipment.

## Table of Contents

- [Business Overview](#business-overview)
- [High-Level Architecture](#high-level-architecture)
- [Technology Stack](#technology-stack)
- [API Endpoints](#api-endpoints)
- [Event Publishing](#event-publishing)
- [Developer's Guide](#developers-guide)
  - [Getting Started](#getting-started)
  - [Testing](#testing)
  - [API and Event Specs](#api-and-event-specs)
  - [CI/CD](#cicd)
  - [Observability](#observability)

## Business Overview

The Order Management Service is the backbone of our fulfillment process. A **fulfillment order** represents a request from a seller to pick, pack, and ship items to a customer. This service orchestrates this process by:

- **Receiving and validating** new fulfillment orders from our partners.
- **Providing real-time status updates** as the order moves through the warehouse.
- **Integrating with other services** through domain events, such as inventory and shipping.
- **Ensuring data consistency and reliability** through patterns like the transactional outbox.

This service is designed to be highly available and scalable to support our growing business needs.

## High-Level Architecture

This service follows the **Hexagonal Architecture** (Ports and Adapters) pattern to isolate the core business logic from external concerns.

```
+---------------------------------------------------------------------------------+
|                                Order Management Service                         |
|                                                                                 |
|    +----------------------+      +-------------------+      +-----------------+ |
|    |      Interfaces      |----->|    Application    |----->|      Domain     | |
|    | (REST Controllers)   |      |     Services      |      | (Business Logic)| |
|    +----------------------+      +-------------------+      +-----------------+ |
|             ^                            |                            |         |
|             |                            v                            v         |
|    +----------------------+      +-------------------+      +-----------------+ |
|    |      Infrastructure  |      |   Infrastructure  |      |  Infrastructure | |
|    | (Spring Web)         |      | (Kafka, Outbox)   |      | (MongoDB)       | |
|    +----------------------+      +-------------------+      +-----------------+ |
|                                                                                 |
+---------------------------------------------------------------------------------+
       ^               |                ^                |               ^
       |               v                |                v               |
+--------------+ +----------------+ +----------------+ +-------------+ +-------------+
|   API Gateway  | |      UI        | |      Kafka     | |   MongoDB   | | 3rd Party   |
+--------------+ +----------------+ +----------------+ +-------------+ +-------------+
```

- **Domain Layer**: Contains the core business logic, entities, and value objects.
- **Application Layer**: Orchestrates the business logic and handles application-level tasks.
- **Interfaces Layer**: Exposes the application's capabilities to the outside world (e.g., REST API).
- **Infrastructure Layer**: Implements the ports defined by the application layer (e.g., repositories, event publishers).

For a more detailed diagram, see the [architecture documentation](./docs/architecture.md).

## Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.3.3
- **Database**: MongoDB
- **Messaging**: Apache Kafka
- **Event Standard**: CloudEvents
- **Build Tool**: Maven

## API Endpoints

### Create Fulfillment Order

`POST /fulfillment_orders`

Creates a new fulfillment order. Returns `202 Accepted` on success or `409 Conflict` if an order with the same `sellerFulfillmentOrderId` already exists.

### Get Fulfillment Order by ID

`GET /fulfillment_orders/{order_id}`

Retrieves a fulfillment order by its system-generated ID. Returns `200 OK` with the order details or `404 Not Found`.

### Cancel Fulfillment Order

`POST /fulfillment_orders/{order_id}/cancel`

Cancels a fulfillment order. Returns `202 Accepted` on success or `400 Bad Request` if the cancellation is not allowed.

## Event Publishing

The service publishes domain events to a Kafka topic in CloudEvents format. Key events include:

- `com.example.fulfillment.order.received`
- `com.example.fulfillment.order.validated`
- `com.example.fulfillment.order.invalidated`
- `com.example.fulfillment.order.cancelled`

Events are published using the transactional outbox pattern to ensure at-least-once delivery.

## Developer's Guide

### Getting Started

#### Prerequisites

- Java 21
- Maven 3.6+
- Docker and Docker Compose

#### Building the Application

```bash
mvn clean install
```

#### Running with Docker Compose (Recommended)

The easiest way to run the application and its dependencies is with Docker Compose:

```bash
docker-compose up --build
```

This will start:
- The Order Management Service on port `8080`
- MongoDB on port `27017`
- Kafka on port `9092`

For development with live reloading, use:

```bash
docker-compose up
```

#### Running in Production

For production, use the `docker-compose.prod.yml` file:

```bash
# Copy and configure the .env file
cp .env.example .env
# Edit .env with secure passwords

# Run in detached mode
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Testing

#### Unit and Integration Tests

Run all tests with:

```bash
mvn verify
```

### API and Event Specs

- **OpenAPI Spec**: View the API documentation at `http://localhost:8080/swagger-ui.html` or in the `openapi.yaml` file.
- **AsyncAPI Spec**: The event-driven architecture is documented in the `asyncapi.yaml` file.
- **Postman Collection**: A Postman collection is available at `fulfillment-order-management.postman_collection.json` to help you test the API.

### CI/CD

This project uses GitHub Actions for CI/CD:

- **CI Pipeline**: Builds and tests on every push.
- **CD Staging/Production**: Deploys to the respective environments.
- **Security Scanning**: Regular security scans with OWASP Dependency Check and SpotBugs.

See [.github/README.md](.github/README.md) for more details.

### Observability

- **Logging**: Structured JSON logs are in the `logs/` directory.
- **Distributed Tracing**: Implemented with Micrometer Tracing.
- **Metrics**: Prometheus metrics at `http://localhost:8081/actuator/prometheus`.
- **Health Checks**: Health endpoints at `http://localhost:8081/actuator/health`.

For more details, see [observability-README.md](src/main/resources/observability-README.md).