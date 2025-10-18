---
layout: default
title: Home
---

# Order Management Service Documentation

Fulfillment order lifecycle management service with event-driven integration, hexagonal architecture, and CloudEvents support.

## Overview

The Order Management Service orchestrates the complete lifecycle of fulfillment orders within the Paklog platform. This bounded context receives orders from sellers, validates them, manages state transitions through picking, packing, and shipping, and publishes domain events to integrate with downstream services.

## Quick Links

### Getting Started
- [Developer Guide](developer-guide.md) - Complete developer onboarding and reference
- [Architecture Overview](architecture.md) - System architecture description

### Architecture & Design
- [Overall Architecture Diagram](overall-architecture-diagram.md) - High-level system view
- [Aggregates Diagram](aggregates-diagram.md) - Domain aggregates
- [Entities & Value Objects](entities-value-objects-diagram.md) - Domain model details
- [Application Services](application-services-diagram.md) - Service layer
- [DDD Components](ddd-components.md) - Domain-Driven Design elements

## Technology Stack

- **Java 21** - Programming language
- **Spring Boot 3.3.3** - Application framework
- **MongoDB** - Document database
- **Apache Kafka** - Event streaming
- **CloudEvents 2.5.0** - Event standard

## Key Features

- Order lifecycle orchestration
- Event-driven integration
- Hexagonal architecture
- State machine pattern
- CloudEvents support
- Transactional outbox

## Domain Model

### Aggregates
- **FulfillmentOrder** - Complete order lifecycle management

### Entities
- **OrderItem** - Individual order line items
- **OutboxEvent** - Pending event publication

### Value Objects
- **Address** - Shipping address
- **FulfillmentOrderStatus** - Order state

### Order Lifecycle

```
RECEIVED → VALIDATED → RELEASED → PICKING →
PACKING_IN_PROGRESS → READY_TO_SHIP → SHIPPED → DELIVERED

Cancellation possible at any stage before SHIPPED
```

## Domain Events

- **FulfillmentOrderReceivedEvent** - New order received
- **FulfillmentOrderValidatedEvent** - Order validated
- **FulfillmentOrderReleasedEvent** - Released to warehouse
- **FulfillmentOrderPickingCompletedEvent** - Picking done
- **FulfillmentOrderPackingCompletedEvent** - Packing done
- **FulfillmentOrderShippedEvent** - Order shipped
- **FulfillmentOrderCancelledEvent** - Order cancelled

## Architecture Patterns

- **Hexagonal Architecture** - Ports and adapters
- **Domain-Driven Design** - Rich domain model
- **Event-Driven Architecture** - Async integration
- **State Pattern** - Order lifecycle management
- **Transactional Outbox** - Reliable event delivery

## API Endpoints

- `POST /fulfillment_orders` - Create order
- `GET /fulfillment_orders/{orderId}` - Get order
- `POST /fulfillment_orders/{orderId}/cancel` - Cancel order
- `GET /fulfillment_orders` - List orders

## Getting Started

1. Review the [Developer Guide](developer-guide.md)
2. Understand the [Architecture](architecture.md)
3. Explore [DDD Components](ddd-components.md)
4. Study the [Architecture Diagrams](overall-architecture-diagram.md)

## Integration Points

### Consumes Events From
- Warehouse Operations (picking/packing completion)
- Shipment Transportation (shipment dispatched)

### Publishes Events To
- Warehouse Operations (order released)
- Inventory (order validation)
- Shipment Transportation (ready to ship)

## Contributing

For contribution guidelines, please refer to the main [README](../README.md) in the project root.

## Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/paklog/order-management/issues)
- **Documentation**: Browse the guides in the navigation menu
- **Developer Guide**: See [Developer Guide](developer-guide.md) for detailed information
