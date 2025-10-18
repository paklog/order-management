# Order Management Service

Fulfillment order lifecycle management service with event-driven integration, hexagonal architecture, and CloudEvents support.

## Overview

The Order Management Service orchestrates the complete lifecycle of fulfillment orders within the Paklog platform. This bounded context receives orders from sellers, validates them, manages state transitions through picking, packing, and shipping, and publishes domain events to integrate with downstream services like Warehouse Operations, Inventory, and Shipment Transportation.

## Domain-Driven Design

### Bounded Context
**Order Management & Fulfillment Orchestration** - Manages the lifecycle of fulfillment orders from receipt through shipment completion.

### Core Domain Model

#### Aggregates
- **FulfillmentOrder** - Root aggregate representing a fulfillment order from a seller

#### Entities
- **OrderItem** - Individual line item within an order
- **OutboxEvent** - Event pending publication

#### Value Objects
- **Address** - Shipping address details
- **FulfillmentOrderStatus** - Order status enumeration (RECEIVED, VALIDATED, RELEASED, PICKING, PACKING_IN_PROGRESS, READY_TO_SHIP, SHIPPED, DELIVERED, CANCELLED)

#### Domain Events
- **FulfillmentOrderReceivedEvent** - New order received from seller
- **FulfillmentOrderValidatedEvent** - Order validated and accepted
- **FulfillmentOrderInvalidatedEvent** - Order validation failed
- **FulfillmentOrderReleasedEvent** - Order released to warehouse
- **FulfillmentOrderPickingCompletedEvent** - Picking completed
- **FulfillmentOrderPackingCompletedEvent** - Packing completed
- **FulfillmentOrderShippedEvent** - Order shipped
- **FulfillmentOrderCancelledEvent** - Order cancelled

### Ubiquitous Language
- **Fulfillment Order**: Request from seller to pick, pack, and ship items
- **Seller Fulfillment Order ID**: External identifier from seller's system
- **Order Lifecycle**: Complete journey from receipt to delivery
- **Order Validation**: Business rules verification before processing
- **Order Release**: Making order available to warehouse for picking
- **Outbox Event**: Event awaiting asynchronous publication

## Architecture & Patterns

### Hexagonal Architecture (Ports and Adapters)

```
src/main/java/com/paklog/ordermanagement/
├── domain/                           # Core business logic
│   ├── model/                       # Aggregates, entities, value objects
│   │   ├── FulfillmentOrder.java    # Main aggregate root
│   │   ├── OrderItem.java           # Order line item
│   │   ├── Address.java             # Value object
│   │   └── OutboxEvent.java         # Outbox entity
│   ├── repository/                  # Repository interfaces (ports)
│   └── event/                       # Domain events
├── application/                      # Use cases & orchestration
│   ├── service/                     # Application services
│   ├── command/                     # Commands
│   ├── query/                       # Queries
│   └── port/                        # Application ports
└── infrastructure/                   # External adapters
    ├── persistence/                 # MongoDB repositories
    ├── messaging/                   # Kafka publishers
    ├── web/                         # REST controllers
    ├── outbox/                      # Outbox scheduler
    └── config/                      # Configuration
```

### Design Patterns & Principles
- **Hexagonal Architecture** - Clean separation of domain and infrastructure
- **Domain-Driven Design** - Rich domain model with business invariants
- **Event-Driven Architecture** - Integration via domain events
- **Transactional Outbox Pattern** - Guaranteed event delivery
- **Aggregate Pattern** - Consistency boundaries around FulfillmentOrder
- **State Pattern** - Order lifecycle state management
- **Repository Pattern** - Data access abstraction
- **SOLID Principles** - Maintainable and extensible code

## Technology Stack

### Core Framework
- **Java 21** - Programming language
- **Spring Boot 3.3.3** - Application framework
- **Maven** - Build and dependency management

### Data & Persistence
- **MongoDB** - Document database for aggregates
- **Spring Data MongoDB** - Data access layer

### Messaging & Events
- **Apache Kafka** - Event streaming platform
- **Spring Kafka** - Kafka integration
- **CloudEvents 2.5.0** - Standardized event format

### API & Documentation
- **Spring Web MVC** - REST API framework
- **Bean Validation** - Input validation
- **OpenAPI/Swagger** - API documentation

### Observability
- **Spring Boot Actuator** - Health checks and metrics
- **Micrometer** - Metrics collection
- **Micrometer Tracing** - Distributed tracing
- **Loki Logback Appender** - Log aggregation

### Testing
- **JUnit 5** - Unit testing framework
- **Testcontainers** - Integration testing
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Local development environment

## Standards Applied

### Architectural Standards
- ✅ Hexagonal Architecture (Ports and Adapters)
- ✅ Domain-Driven Design tactical patterns
- ✅ Event-Driven Architecture
- ✅ Microservices architecture
- ✅ RESTful API design
- ✅ State machine pattern for order lifecycle

### Code Quality Standards
- ✅ SOLID principles
- ✅ Clean Code practices
- ✅ Comprehensive unit and integration testing
- ✅ Domain-driven design patterns
- ✅ Immutable value objects
- ✅ Rich domain models with business logic

### Event & Integration Standards
- ✅ CloudEvents specification v1.0
- ✅ Transactional Outbox Pattern
- ✅ At-least-once delivery semantics
- ✅ Event versioning strategy
- ✅ Idempotent event handling

### Observability Standards
- ✅ Structured logging (JSON)
- ✅ Distributed tracing
- ✅ Health check endpoints
- ✅ Prometheus metrics
- ✅ Correlation ID propagation

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/paklog/order-management.git
   cd order-management
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d mongodb kafka
   ```

3. **Build and run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify the service is running**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Using Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f order-management

# Stop all services
docker-compose down
```

## API Documentation

Once running, access the interactive API documentation:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### Key Endpoints

- `POST /fulfillment_orders` - Create new fulfillment order
- `GET /fulfillment_orders/{orderId}` - Get order by ID
- `POST /fulfillment_orders/{orderId}/cancel` - Cancel order
- `GET /fulfillment_orders` - List orders (with filters)
- `GET /fulfillment_orders/seller/{sellerOrderId}` - Get order by seller ID

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run tests with coverage
mvn clean verify jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Configuration

Key configuration properties:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/order_management
  kafka:
    bootstrap-servers: localhost:9092

order-management:
  outbox:
    scheduler:
      enabled: true
      fixed-delay: 5000
  validation:
    strict-mode: true
```

## Event Integration

### Published Events
- `com.paklog.fulfillment.order.received.v1`
- `com.paklog.fulfillment.order.validated.v1`
- `com.paklog.fulfillment.order.invalidated.v1`
- `com.paklog.fulfillment.order.released.v1`
- `com.paklog.fulfillment.order.picking.completed.v1`
- `com.paklog.fulfillment.order.packing.completed.v1`
- `com.paklog.fulfillment.order.shipped.v1`
- `com.paklog.fulfillment.order.cancelled.v1`

### Consumed Events
- `com.paklog.warehouse.picking.completed` - From Warehouse Operations
- `com.paklog.warehouse.packing.completed` - From Warehouse Operations
- `com.paklog.shipment.dispatched` - From Shipment Transportation

### Event Format
All events follow the CloudEvents specification v1.0 and are published via the transactional outbox pattern.

## Order Lifecycle

```
RECEIVED → VALIDATED → RELEASED → PICKING → PACKING_IN_PROGRESS → READY_TO_SHIP → SHIPPED → DELIVERED
    ↓
INVALIDATED
    ↓
CANCELLED (from any state except SHIPPED/DELIVERED)
```

## Monitoring

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus
- **Info**: http://localhost:8080/actuator/info

## Contributing

1. Follow hexagonal architecture principles
2. Implement domain logic in domain layer
3. Maintain aggregate consistency boundaries
4. Use transactional outbox for event publishing
5. Respect order lifecycle state transitions
6. Write comprehensive tests including domain model tests
7. Document domain concepts using ubiquitous language
8. Follow existing code style and conventions

## License

Copyright © 2024 Paklog. All rights reserved.
