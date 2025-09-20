# Order Management Service - Architecture

This document provides a comprehensive overview of the Order Management Service architecture, following the Hexagonal Architecture (Ports and Adapters) pattern.

## 1. High-Level System Architecture

This diagram shows the overall structure of the service and its interactions with external systems.

```mermaid
graph TB
    subgraph "External Systems"
        ECOM[E-commerce Platform]
        WMS[Warehouse Management System]
        NOTIF[Notification Service]
    end

    subgraph "Order Management Service"
        subgraph "Interfaces Layer (Adapters)"
            REST[REST Controllers]
            KAFKA_CONSUMER[Kafka Consumers]
        end

        subgraph "Application Layer"
            APP_SERVICES[Application Services]
            PORTS[Domain Ports]
        end

        subgraph "Domain Layer (Core)"
            AGGREGATES[Aggregates]
            ENTITIES[Entities]
            VALUE_OBJECTS[Value Objects]
            DOMAIN_EVENTS[Domain Events]
        end

        subgraph "Infrastructure Layer (Adapters)"
            REPOS[Repository Implementations]
            EVENT_PUB[Event Publishers]
        end
    end

    subgraph "Data & Messaging"
        MONGODB[MongoDB]
        KAFKA[Apache Kafka]
    end

    %% Interactions
    ECOM -->|HTTP/JSON| REST
    WMS -->|Consumes Events| KAFKA
    NOTIF -->|Consumes Events| KAFKA

    REST -->|Calls| APP_SERVICES
    KAFKA_CONSUMER -->|Calls| APP_SERVICES

    APP_SERVICES -->|Uses| PORTS
    APP_SERVICES -->|Orchestrates| AGGREGATES

    PORTS -.->|Implemented by| REPOS
    PORTS -.->|Implemented by| EVENT_PUB

    REPOS -->|Persists to| MONGODB
    EVENT_PUB -->|Publishes to| KAFKA
```

## 2. Application Services and Interactions

This section details the application services and how they interact with other layers.

```mermaid
graph TB
    subgraph "Presentation Layer"
        FC[FulfillmentOrderController]
    end

    subgraph "Application Services"
        FOS[FulfillmentOrderService]
        EPS[EventPublisherService]
        SCHED[OutboxEventPublisherScheduler]
    end

    subgraph "Domain Model"
        FO[FulfillmentOrder Aggregate]
        OE[OutboxEvent Aggregate]
    end

    subgraph "Infrastructure (Ports & Adapters)"
        FOR[FulfillmentOrderRepository Port]
        OER[OutboxEventRepository Port]
        MFOR[MongoFulfillmentOrderRepository Adapter]
        KT[KafkaTemplate Adapter]
    end

    %% Dependencies
    FC -->|Calls| FOS
    FOS -->|Uses| FOR
    FOS -->|Uses| EPS
    EPS -->|Uses| OER
    EPS -->|Uses| KT
    SCHED -->|Triggers| EPS

    %% Implementation
    FOR -.->|Implemented by| MFOR
    OER -.->|Implemented by| MFOR
```

### FulfillmentOrderService
- **Responsibility:** Orchestrates the lifecycle of fulfillment orders.
- **Key Operations:** Creating, validating, and canceling orders.
- **Dependencies:** `FulfillmentOrderRepository`, `EventPublisherService`.

### EventPublisherService
- **Responsibility:** Publishes domain events using the transactional outbox pattern.
- **Key Operations:** Saving events to the outbox, publishing events to Kafka.
- **Dependencies:** `OutboxEventRepository`, `KafkaTemplate`.

### OutboxEventPublisherScheduler
- **Responsibility:** Periodically triggers the `EventPublisherService` to send unpublished events.
- **Key Operations:** Runs on a schedule to ensure reliable event delivery.
- **Dependencies:** `EventPublisherService`.


## 3. Data Flow: Order Creation

This sequence diagram illustrates the process of creating a new fulfillment order.

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant AppService
    participant Aggregate
    participant Repository
    participant EventService
    participant Outbox
    participant Kafka

    Client->>Controller: POST /fulfillment_orders
    Controller->>AppService: createOrder(request)
    AppService->>Aggregate: new FulfillmentOrder()
    AppService->>Aggregate: receive()
    AppService->>Repository: save(order)
    AppService->>EventService: publishEvent(orderReceivedEvent)
    EventService->>Outbox: save(outboxEvent)

    Note over EventService,Kafka: Background Process
    EventService->>Outbox: findUnpublished()
    EventService->>Kafka: publish(cloudEvent)
    EventService->>Outbox: markPublished()
```

## 4. Technology Stack

- **Language:** Java 17
- **Framework:** Spring Boot
- **Database:** MongoDB
- **Messaging:** Apache Kafka
- **Build:** Maven
