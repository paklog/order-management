# Order Management - Application Services Diagram

This diagram shows the application layer services, their responsibilities, and interactions with the domain and infrastructure layers.

```mermaid
graph TB
    subgraph "Presentation Layer"
        FC[FulfillmentOrderController<br/>REST Endpoints]
    end

    subgraph "Application Layer"
        FOS[FulfillmentOrderService<br/><<Application Service>>]
        EPS[EventPublisherService<br/><<Application Service>>]
        OEPS[OutboxEventPublisherScheduler<br/><<Application Service>>]
    end

    subgraph "Domain Layer"
        FO[FulfillmentOrder<br/><<Aggregate Root>>]
        OE[OutboxEvent<br/><<Aggregate Root>>]
        DE[Domain Events]
    end

    subgraph "Infrastructure Layer"
        FOR[FulfillmentOrderRepository<br/><<Port>>]
        OER[OutboxEventRepository<br/><<Port>>]
        MFOR[MongoFulfillmentOrderRepository<br/><<Adapter>>]
        KT[KafkaTemplate<br/><<Adapter>>]
    end

    subgraph "External Systems"
        KAFKA[Apache Kafka<br/>Message Broker]
        MONGO[MongoDB<br/>Database]
    end

    %% Presentation to Application
    FC -->|calls| FOS

    %% Application Service Interactions
    FOS -->|uses| EPS
    FOS -->|persists| FO
    EPS -->|persists| OE
    OEPS -->|publishes| EPS

    %% Application to Domain
    FOS -->|orchestrates| FO
    EPS -->|handles| DE

    %% Application to Infrastructure
    FOS -->|uses| FOR
    EPS -->|uses| OER
    OEPS -->|uses| OER

    %% Infrastructure Implementations
    FOR -.->|implemented by| MFOR
    EPS -->|uses| KT

    %% External Dependencies
    MFOR -->|stores data| MONGO
    KT -->|publishes to| KAFKA

    %% Styling
    classDef presentationLayer fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef applicationLayer fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef domainLayer fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    classDef infrastructureLayer fill:#fff8e1,stroke:#f57c00,stroke-width:2px
    classDef externalSystem fill:#fce4ec,stroke:#c2185b,stroke-width:2px

    class FC presentationLayer
    class FOS,EPS,OEPS applicationLayer
    class FO,OE,DE domainLayer
    class FOR,OER,MFOR,KT infrastructureLayer
    class KAFKA,MONGO externalSystem
```

## Application Services Details

### FulfillmentOrderService
**Purpose**: Orchestrates fulfillment order business operations
```mermaid
sequenceDiagram
    participant Controller
    participant FulfillmentOrderService
    participant FulfillmentOrder
    participant EventPublisherService
    participant Repository

    Controller->>FulfillmentOrderService: createOrder(order)
    FulfillmentOrderService->>Repository: findBySellerFulfillmentOrderId()
    Repository-->>FulfillmentOrderService: Optional.empty()
    FulfillmentOrderService->>FulfillmentOrder: receive()
    FulfillmentOrder-->>FulfillmentOrderService: status = RECEIVED
    FulfillmentOrderService->>Repository: save(order)
    Repository-->>FulfillmentOrderService: savedOrder
    FulfillmentOrderService->>EventPublisherService: publishEvent(orderReceivedEvent)
    EventPublisherService-->>FulfillmentOrderService: success
    FulfillmentOrderService-->>Controller: savedOrder
```

**Key Responsibilities**:
- Order creation with uniqueness validation
- Order retrieval by ID
- Order cancellation with business rule validation
- Transaction boundary management
- Event publishing coordination

### EventPublisherService
**Purpose**: Handles domain event publishing using transactional outbox pattern
```mermaid
sequenceDiagram
    participant ApplicationService
    participant EventPublisherService
    participant OutboxRepository
    participant Scheduler
    participant KafkaTemplate

    ApplicationService->>EventPublisherService: publishEvent(domainEvent)
    EventPublisherService->>EventPublisherService: convertToCloudEvent()
    EventPublisherService->>OutboxRepository: save(outboxEvent)
    OutboxRepository-->>EventPublisherService: saved

    Note over Scheduler: Background Process
    Scheduler->>EventPublisherService: publishOutboxEvents()
    EventPublisherService->>OutboxRepository: findByPublishedFalse()
    OutboxRepository-->>EventPublisherService: unpublishedEvents
    loop For each event
        EventPublisherService->>KafkaTemplate: send(topic, eventData)
        KafkaTemplate-->>EventPublisherService: success
        EventPublisherService->>OutboxRepository: markAsPublished()
    end
```

**Key Responsibilities**:
- CloudEvents format conversion
- Transactional outbox pattern implementation
- Retry mechanism for failed publications
- At-least-once delivery guarantee

### OutboxEventPublisherScheduler
**Purpose**: Background scheduler for reliable event delivery
```mermaid
graph LR
    TIMER[Timer Trigger<br/>@Scheduled] -->|every 30s| SCHEDULER[OutboxEventPublisherScheduler]
    SCHEDULER -->|publishOutboxEvents| EPS[EventPublisherService]
    EPS -->|query unpublished| REPO[OutboxEventRepository]
    EPS -->|publish to| KAFKA[Kafka Topic]
    EPS -->|mark published| REPO
```

**Key Responsibilities**:
- Periodic execution of event publishing
- Error handling and logging
- Monitoring unpublished events
- Dead letter queue handling (future enhancement)

## Service Interaction Patterns

### Command Pattern
```mermaid
classDiagram
    class CreateOrderCommand {
        +UUID orderId
        +String sellerFulfillmentOrderId
        +Address destinationAddress
        +List~OrderItem~ items
    }

    class CancelOrderCommand {
        +UUID orderId
        +String cancellationReason
    }

    class FulfillmentOrderService {
        +createOrder(FulfillmentOrder) FulfillmentOrder
        +cancelOrder(UUID, String) FulfillmentOrder
        +getOrderById(UUID) Optional~FulfillmentOrder~
    }

    CreateOrderCommand ..> FulfillmentOrderService : handled by
    CancelOrderCommand ..> FulfillmentOrderService : handled by
```

### Event Sourcing Support (Future)
```mermaid
graph TB
    subgraph "Current Implementation"
        STATE[State-based Storage]
        EVENTS[Event Publishing]
    end

    subgraph "Future Event Sourcing"
        ES[Event Store]
        PROJ[Projections]
        SNAP[Snapshots]
    end

    STATE -.->|migration path| ES
    EVENTS -.->|already publishing| ES
    ES -->|rebuild from| PROJ
    ES -->|performance| SNAP
```

## Cross-Cutting Concerns

### Transaction Management
```mermaid
graph LR
    subgraph "Transaction Boundary"
        TXN[@Transactional]
        ORDER[Order State Change]
        EVENT[Event Storage]
    end

    TXN -->|ensures| ORDER
    TXN -->|ensures| EVENT
    TXN -->|rollback on failure| ORDER
    TXN -->|rollback on failure| EVENT
```

### Error Handling
```mermaid
graph TB
    REQUEST[Incoming Request] -->|try| SERVICE[Application Service]
    SERVICE -->|IllegalStateException| CONFLICT[409 Conflict]
    SERVICE -->|IllegalArgumentException| NOTFOUND[404 Not Found]
    SERVICE -->|RuntimeException| ERROR[500 Internal Error]
    SERVICE -->|Success| SUCCESS[200/202 Success]

    style CONFLICT fill:#ffcdd2
    style NOTFOUND fill:#fff3e0
    style ERROR fill:#ffebee
    style SUCCESS fill:#e8f5e8
```

### Monitoring and Observability
```mermaid
graph TB
    subgraph "Application Services"
        METRICS[Micrometer Metrics]
        LOGS[Structured Logging]
        TRACES[Distributed Tracing]
    end

    subgraph "Monitoring Points"
        ORDERS[Order Creation Rate]
        EVENTS[Event Publishing Lag]
        ERRORS[Error Rate by Type]
        LATENCY[Service Response Time]
    end

    METRICS -->|exposes| ORDERS
    METRICS -->|exposes| EVENTS
    METRICS -->|exposes| ERRORS
    METRICS -->|exposes| LATENCY

    LOGS -->|correlates with| TRACES
    TRACES -->|spans across| ORDERS
```

## Configuration and Dependencies

### Spring Configuration
```java
@Configuration
@EnableTransactionManagement
@EnableScheduling
public class ApplicationConfig {

    @Bean
    public FulfillmentOrderService fulfillmentOrderService(
            FulfillmentOrderRepository repository,
            EventPublisherService eventPublisher) {
        return new FulfillmentOrderService(repository, eventPublisher);
    }

    @Bean
    public EventPublisherService eventPublisherService(
            OutboxEventRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        return new EventPublisherService(outboxRepository, kafkaTemplate, objectMapper);
    }
}
```

### Dependency Injection Flow
```mermaid
graph TB
    CONFIG[Application Configuration] -->|creates| FOS[FulfillmentOrderService]
    CONFIG -->|creates| EPS[EventPublisherService]
    CONFIG -->|creates| SCHEDULER[OutboxEventPublisherScheduler]

    FOS -->|depends on| REPO[FulfillmentOrderRepository]
    FOS -->|depends on| EPS
    EPS -->|depends on| OUTBOX[OutboxEventRepository]
    EPS -->|depends on| KAFKA[KafkaTemplate]
    SCHEDULER -->|depends on| EPS

    style CONFIG fill:#e1f5fe
    style FOS fill:#f3e5f5
    style EPS fill:#f3e5f5
    style SCHEDULER fill:#f3e5f5
```