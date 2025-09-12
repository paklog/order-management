
# Fulfillment Order Aggregate

This diagram illustrates the `FulfillmentOrder` aggregate, its entities, and value objects.

- **`FulfillmentOrder`**: The Aggregate Root.
- **`OrderItem`**: An Entity within the aggregate.
- **`Address`**: A Value Object.
- **`FulfillmentOrderStatus`**: An Enum Value Object representing the state of the order.

```mermaid
classDiagram
    class FulfillmentOrder {
        <<AggregateRoot>>
        -UUID orderId
        -String sellerFulfillmentOrderId
        -FulfillmentOrderStatus status
        -Address destinationAddress
        -List~OrderItem~ items
        +receive()
        +validate()
        +invalidate(String reason)
        +cancel(String reason)
    }

    class OrderItem {
        <<Entity>>
        -String sellerSku
        -String sellerFulfillmentOrderItemId
        -Integer quantity
    }

    class Address {
        <<ValueObject>>
        -String name
        -String addressLine1
        -String city
        -String postalCode
        -String countryCode
    }

    class FulfillmentOrderStatus {
        <<Enumeration>>
        NEW
        RECEIVED
        VALIDATED
        INVALIDATED
        CANCELLED
        SHIPPED
    }

    FulfillmentOrder "1" *-- "N" OrderItem : contains
    FulfillmentOrder "1" *-- "1" Address : has destination
    FulfillmentOrder "1" -- "1" FulfillmentOrderStatus : has status
```
