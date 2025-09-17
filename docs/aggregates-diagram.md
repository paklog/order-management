# Order Management - Aggregates Diagram

This diagram shows the main aggregates in the Order Management bounded context, their relationships, and key behaviors.

```mermaid
graph TB
    subgraph "Order Management Bounded Context"
        subgraph "FulfillmentOrder Aggregate"
            FO[FulfillmentOrder<br/><<Aggregate Root>>]

            subgraph "Entities"
                OI[OrderItem<br/><<Entity>>]
            end

            subgraph "Value Objects"
                ADDR[Address<br/><<Value Object>>]
                STATUS[FulfillmentOrderStatus<br/><<Enum>>]
            end

            subgraph "Domain Events"
                FOR[FulfillmentOrderReceivedEvent]
                FOV[FulfillmentOrderValidatedEvent]
                FOI[FulfillmentOrderInvalidatedEvent]
                FOC[FulfillmentOrderCancelledEvent]
                FOPS[FulfillmentOrderPickingCompletedEvent]
                FOPA[FulfillmentOrderPackingCompletedEvent]
                FOS[FulfillmentOrderShippedEvent]
                FORE[FulfillmentOrderReleasedEvent]
            end
        end

        subgraph "Outbox Aggregate"
            OE[OutboxEvent<br/><<Aggregate Root>>]
        end
    end

    %% Relationships
    FO -->|contains| OI
    FO -->|has| ADDR
    FO -->|has| STATUS
    FO -->|publishes| FOR
    FO -->|publishes| FOV
    FO -->|publishes| FOI
    FO -->|publishes| FOC
    FO -->|publishes| FOPS
    FO -->|publishes| FOPA
    FO -->|publishes| FOS
    FO -->|publishes| FORE

    %% Event Flow
    FOR -.->|stored in| OE
    FOV -.->|stored in| OE
    FOI -.->|stored in| OE
    FOC -.->|stored in| OE
    FOPS -.->|stored in| OE
    FOPA -.->|stored in| OE
    FOS -.->|stored in| OE
    FORE -.->|stored in| OE

    %% Styling
    classDef aggregateRoot fill:#e1f5fe,stroke:#01579b,stroke-width:3px
    classDef entity fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef valueObject fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    classDef domainEvent fill:#fff3e0,stroke:#e65100,stroke-width:2px

    class FO,OE aggregateRoot
    class OI entity
    class ADDR,STATUS valueObject
    class FOR,FOV,FOI,FOC,FOPS,FOPA,FOS,FORE domainEvent
```

## Aggregate Details

### FulfillmentOrder Aggregate
**Aggregate Root**: `FulfillmentOrder`
- **Purpose**: Manages the complete lifecycle of a fulfillment order
- **Invariants**:
  - Cannot cancel shipped orders
  - Must have at least one order item
  - Unique sellerFulfillmentOrderId per seller
- **Key Behaviors**:
  - `receive()`: Transition from NEW to RECEIVED
  - `validate()`: Transition from RECEIVED to VALIDATED
  - `invalidate()`: Transition from RECEIVED to INVALIDATED
  - `cancel()`: Transition to CANCELLED (if not shipped)

### OutboxEvent Aggregate
**Aggregate Root**: `OutboxEvent`
- **Purpose**: Implements transactional outbox pattern for reliable event publishing
- **Invariants**:
  - Events are immutable once created
  - Published flag ensures idempotency
- **Key Behaviors**:
  - Store domain events transactionally
  - Track publication status
  - Enable retry mechanism

## State Transitions

```mermaid
stateDiagram-v2
    [*] --> NEW : Create Order
    NEW --> RECEIVED : receive()
    RECEIVED --> VALIDATED : validate()
    RECEIVED --> INVALIDATED : invalidate()
    VALIDATED --> CANCELLED : cancel()
    RECEIVED --> CANCELLED : cancel()
    VALIDATED --> SHIPPED : ship()
    CANCELLED --> [*]
    SHIPPED --> [*]
    INVALIDATED --> [*]
```

## Business Rules

1. **Order Uniqueness**: Each `sellerFulfillmentOrderId` must be unique per seller
2. **State Validation**: Orders can only transition through valid state paths
3. **Cancellation Rules**: Orders cannot be cancelled once shipped
4. **Event Publishing**: All state changes must publish corresponding domain events
5. **Transactional Consistency**: Order state and events must be updated atomically