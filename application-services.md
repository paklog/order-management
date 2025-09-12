
# Application Services and Repository

This diagram shows the `FulfillmentOrderService` application service and its dependencies on the `FulfillmentOrderRepository` interface and the `EventPublisherService`.

- **`FulfillmentOrderService`**: Application Service for orchestrating order-related use cases.
- **`FulfillmentOrderRepository`**: Domain Repository interface for persistence.
- **`EventPublisherService`**: Application/Infrastructure service for publishing domain events.

```mermaid
classDiagram
    class FulfillmentOrderService {
        <<ApplicationService>>
        +createOrder(FulfillmentOrder order)
        +getOrderById(UUID orderId)
        +cancelOrder(UUID orderId, String reason)
    }

    class FulfillmentOrderRepository {
        <<Repository>>
        +save(FulfillmentOrder order)
        +findById(UUID orderId)
        +findBySellerFulfillmentOrderId(String id)
    }

    class EventPublisherService {
        <<Service>>
        +publishEvent(FulfillmentOrderEvent event)
        +publishOutboxEvents()
    }

    FulfillmentOrderService ..> FulfillmentOrderRepository : uses
    FulfillmentOrderService ..> EventPublisherService : uses
```
