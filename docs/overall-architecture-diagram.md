# Order Management - Overall Architecture Diagram

This diagram provides a comprehensive view of the Order Management Service architecture, showing how all components work together following Hexagonal Architecture (Ports and Adapters) pattern.

## High-Level System Architecture

```mermaid
graph TB
    subgraph "External Systems"
        ECOM[E-commerce Platform]
        WMS[Warehouse Management System]
        NOTIF[Notification Service]
        INV[Inventory Service]
    end

    subgraph "Order Management Service"
        subgraph "Interfaces Layer (Adapters)"
            REST[REST Controllers]
            DTO[DTOs]
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
            BUSINESS_RULES[Business Rules]
        end

        subgraph "Infrastructure Layer (Adapters)"
            REPOS[Repository Implementations]
            EVENT_PUB[Event Publishers]
            EXTERNAL_CLIENTS[External Service Clients]
        end
    end

    subgraph "Data & Messaging"
        MONGODB[MongoDB]
        KAFKA[Apache Kafka]
    end

    %% External interactions
    ECOM -->|HTTP/JSON| REST
    WMS -->|consumes events| KAFKA
    NOTIF -->|consumes events| KAFKA
    INV <-->|HTTP/JSON| EXTERNAL_CLIENTS

    %% Layer interactions (following Hexagonal Architecture)
    REST -->|calls| APP_SERVICES
    KAFKA_CONSUMER -->|calls| APP_SERVICES

    APP_SERVICES -->|uses| PORTS
    APP_SERVICES -->|orchestrates| AGGREGATES

    AGGREGATES -->|publishes| DOMAIN_EVENTS
    AGGREGATES -->|contains| ENTITIES
    AGGREGATES -->|uses| VALUE_OBJECTS
    AGGREGATES -->|enforces| BUSINESS_RULES

    PORTS -.->|implemented by| REPOS
    PORTS -.->|implemented by| EVENT_PUB
    PORTS -.->|implemented by| EXTERNAL_CLIENTS

    REPOS -->|persists to| MONGODB
    EVENT_PUB -->|publishes to| KAFKA

    %% Styling
    classDef externalSystem fill:#ffebee,stroke:#d32f2f,stroke-width:2px
    classDef interfaceLayer fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef applicationLayer fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef domainLayer fill:#e8f5e8,stroke:#388e3c,stroke-width:3px
    classDef infrastructureLayer fill:#fff8e1,stroke:#f57c00,stroke-width:2px
    classDef dataLayer fill:#fce4ec,stroke:#c2185b,stroke-width:2px

    class ECOM,WMS,NOTIF,INV externalSystem
    class REST,DTO,KAFKA_CONSUMER interfaceLayer
    class APP_SERVICES,PORTS applicationLayer
    class AGGREGATES,ENTITIES,VALUE_OBJECTS,DOMAIN_EVENTS,BUSINESS_RULES domainLayer
    class REPOS,EVENT_PUB,EXTERNAL_CLIENTS infrastructureLayer
    class MONGODB,KAFKA dataLayer
```

## Detailed Component Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        FC[FulfillmentOrderController]
        HEALTH[Health Check Controller]
        METRICS[Metrics Endpoint]
    end

    subgraph "Application Services"
        FOS[FulfillmentOrderService]
        EPS[EventPublisherService]
        SCHED[OutboxEventPublisherScheduler]
    end

    subgraph "Domain Model"
        subgraph "FulfillmentOrder Aggregate"
            FO[FulfillmentOrder]
            OI[OrderItem]
            ADDR[Address]
            STATUS[FulfillmentOrderStatus]
        end

        subgraph "Outbox Aggregate"
            OE[OutboxEvent]
        end

        subgraph "Domain Events"
            FOR_EVENT[FulfillmentOrderReceivedEvent]
            FOV_EVENT[FulfillmentOrderValidatedEvent]
            FOC_EVENT[FulfillmentOrderCancelledEvent]
            FOS_EVENT[FulfillmentOrderShippedEvent]
        end
    end

    subgraph "Infrastructure"
        FOR_REPO[FulfillmentOrderRepository]
        OE_REPO[OutboxEventRepository]
        MONGO_IMPL[MongoFulfillmentOrderRepository]
        KAFKA_TEMPLATE[KafkaTemplate]
    end

    subgraph "External Dependencies"
        MONGO_DB[(MongoDB)]
        KAFKA_BROKER[Kafka Cluster]
        ACTUATOR[Spring Actuator]
    end

    %% Service Dependencies
    FC -->|@Autowired| FOS
    FOS -->|@Autowired| EPS
    FOS -->|@Autowired| FOR_REPO
    EPS -->|@Autowired| OE_REPO
    EPS -->|@Autowired| KAFKA_TEMPLATE
    SCHED -->|@Autowired| EPS

    %% Domain Model Relationships
    FO -->|contains| OI
    FO -->|has| ADDR
    FO -->|has| STATUS
    FO -->|publishes| FOR_EVENT
    FO -->|publishes| FOV_EVENT
    FO -->|publishes| FOC_EVENT
    FO -->|publishes| FOS_EVENT

    %% Infrastructure Implementations
    FOR_REPO -.->|interface| MONGO_IMPL
    MONGO_IMPL -->|stores in| MONGO_DB
    KAFKA_TEMPLATE -->|publishes to| KAFKA_BROKER

    %% Event Flow
    FOR_EVENT -.->|stored as| OE
    FOV_EVENT -.->|stored as| OE
    FOC_EVENT -.->|stored as| OE
    FOS_EVENT -.->|stored as| OE

    %% Monitoring
    HEALTH -->|checks| MONGO_DB
    HEALTH -->|checks| KAFKA_BROKER
    METRICS -->|exposes| ACTUATOR
```

## Data Flow and Event Processing

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
    participant ExternalSystem

    Client->>Controller: POST /fulfillment_orders
    Controller->>AppService: createOrder(request)

    AppService->>Repository: findBySellerOrderId()
    Repository-->>AppService: Optional.empty()

    AppService->>Aggregate: new FulfillmentOrder()
    AppService->>Aggregate: receive()
    Aggregate-->>AppService: status = RECEIVED

    AppService->>Repository: save(order)
    Repository-->>AppService: savedOrder

    AppService->>EventService: publishEvent(orderReceivedEvent)
    EventService->>Outbox: save(outboxEvent)
    Outbox-->>EventService: stored
    EventService-->>AppService: success

    AppService-->>Controller: savedOrder
    Controller-->>Client: 202 Accepted

    Note over EventService,Kafka: Background Process
    EventService->>Outbox: findUnpublished()
    Outbox-->>EventService: events[]
    EventService->>Kafka: publish(cloudEvent)
    Kafka-->>EventService: ack
    EventService->>Outbox: markPublished()

    Kafka->>ExternalSystem: orderReceivedEvent
    ExternalSystem-->>Kafka: processed
```

## Technology Stack Mapping

```mermaid
graph TB
    subgraph "Runtime Environment"
        JVM[Java 17 JVM]
        SPRING[Spring Boot 3.3.3]
    end

    subgraph "Frameworks & Libraries"
        SPRING_WEB[Spring Web]
        SPRING_DATA[Spring Data MongoDB]
        SPRING_KAFKA[Spring Kafka]
        CLOUD_EVENTS[CloudEvents SDK]
        MICROMETER[Micrometer]
    end

    subgraph "Infrastructure Components"
        MONGODB_DRIVER[MongoDB Driver]
        KAFKA_CLIENT[Kafka Client]
        ACTUATOR[Spring Actuator]
        LOGBACK[Logback Logging]
    end

    subgraph "External Systems"
        MONGO[(MongoDB 4.4+)]
        KAFKA_CLUSTER[Kafka Cluster]
        PROMETHEUS[Prometheus]
    end

    JVM -->|runs| SPRING
    SPRING -->|includes| SPRING_WEB
    SPRING -->|includes| SPRING_DATA
    SPRING -->|includes| SPRING_KAFKA
    SPRING -->|includes| CLOUD_EVENTS
    SPRING -->|includes| MICROMETER

    SPRING_DATA -->|uses| MONGODB_DRIVER
    SPRING_KAFKA -->|uses| KAFKA_CLIENT
    MICROMETER -->|uses| ACTUATOR
    SPRING -->|uses| LOGBACK

    MONGODB_DRIVER -->|connects to| MONGO
    KAFKA_CLIENT -->|connects to| KAFKA_CLUSTER
    ACTUATOR -->|exposes metrics to| PROMETHEUS
```

## Deployment Architecture

```mermaid
graph TB
    subgraph "Docker Containers"
        subgraph "Application Container"
            APP[Order Management Service<br/>Port: 8080]
            ACTUATOR_PORT[Actuator<br/>Port: 8081]
        end

        subgraph "Infrastructure Containers"
            MONGO_CONTAINER[MongoDB<br/>Port: 27017]
            KAFKA_CONTAINER[Kafka<br/>Port: 9092]
            ZOOKEEPER[Zookeeper<br/>Port: 2181]
        end
    end

    subgraph "External Services"
        EXTERNAL_API[External APIs]
        MONITORING[Monitoring Stack]
    end

    APP -->|connects to| MONGO_CONTAINER
    APP -->|publishes to| KAFKA_CONTAINER
    KAFKA_CONTAINER -->|depends on| ZOOKEEPER

    EXTERNAL_API -->|calls| APP
    MONITORING -->|scrapes| ACTUATOR_PORT

    %% Network
    APP -.->|docker network| MONGO_CONTAINER
    APP -.->|docker network| KAFKA_CONTAINER
```

## Configuration Management

```mermaid
graph LR
    subgraph "Configuration Sources"
        APP_YML[application.yml]
        ENV_VARS[Environment Variables]
        DOCKER_ENV[Docker Compose Environment]
        SECRETS[Kubernetes Secrets]
    end

    subgraph "Configuration Categories"
        DB_CONFIG[Database Configuration]
        KAFKA_CONFIG[Kafka Configuration]
        LOGGING_CONFIG[Logging Configuration]
        SECURITY_CONFIG[Security Configuration]
    end

    APP_YML -->|provides defaults| DB_CONFIG
    APP_YML -->|provides defaults| KAFKA_CONFIG
    APP_YML -->|provides defaults| LOGGING_CONFIG

    ENV_VARS -->|overrides| DB_CONFIG
    ENV_VARS -->|overrides| KAFKA_CONFIG

    DOCKER_ENV -->|development| ENV_VARS
    SECRETS -->|production| SECURITY_CONFIG

    %% Priority (high to low)
    SECRETS -.->|highest priority| ENV_VARS
    ENV_VARS -.->|medium priority| APP_YML
```

## Quality Attributes

### Scalability
- **Horizontal Scaling**: Stateless application services
- **Database Scaling**: MongoDB replica sets and sharding
- **Event Processing**: Kafka partitioning for parallel processing

### Reliability
- **Transactional Outbox**: Ensures event delivery
- **Retry Mechanisms**: Failed event publishing retry
- **Circuit Breakers**: Protection against external service failures

### Observability
- **Metrics**: Micrometer + Prometheus
- **Logging**: Structured JSON logging
- **Tracing**: Distributed tracing with Spring Cloud Sleuth
- **Health Checks**: Spring Actuator endpoints

### Security
- **Input Validation**: DTO validation and sanitization
- **Authentication**: JWT token validation (future)
- **Authorization**: Role-based access control (future)
- **Audit Trail**: Event sourcing for complete audit log