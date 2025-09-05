# Implementation Plan: Order Management Bounded Context

This document outlines the development tasks required to implement the capabilities of the **Order Management service**, based on the agreed-upon **OpenAPI** and **AsyncAPI** specifications.

---

## Epic 1: Implement Synchronous Order Management API (REST)

**Goal:**  
To build the core synchronous command and query capabilities, allowing clients to create, retrieve, and cancel fulfillment orders via a RESTful interface.

| Task ID | Task Description | Acceptance Criteria |
|---------|-----------------|----------------------|
| **OM-01** | Setup Service Foundation | - A new microservice project (`order-management-service`) is created with a standard project structure.<br>- Basic dependencies for web framework, persistence, and logging are configured.<br>- A CI/CD pipeline is established for automated builds and testing. |
| **OM-02** | Implement FulfillmentOrder Aggregate | - The `FulfillmentOrder` aggregate root, along with `OrderItem` and `Address` value objects, are coded.<br>- A state machine is implemented within the aggregate to enforce the order lifecycle (New, Received, Cancelled, etc.).<br>- Business rule validations (e.g., address validation, item quantity checks) are included in the aggregate's methods. |
| **OM-03** | Implement Persistence Layer | - A database schema is designed to store the `FulfillmentOrder` aggregate.<br>- A repository pattern is implemented to handle the loading and saving of the aggregate to the database. |
| **OM-04** | Implement `POST /fulfillment_orders` Endpoint | - The endpoint is created according to the OpenAPI specification.<br>- The endpoint accepts a `CreateFulfillmentOrderRequest` body.<br>- Logic is implemented to check for an existing `seller_fulfillment_order_id` to ensure idempotency. A **409 Conflict** is returned if a duplicate exists.<br>- On success, a new `FulfillmentOrder` aggregate is created, persisted, and a **202 Accepted** response is returned with the order details. |
| **OM-05** | Implement `GET /fulfillment_orders/{order_id}` Endpoint | - The endpoint is created according to the OpenAPI specification.<br>- It retrieves a `FulfillmentOrder` by its system-generated `order_id` from the repository.<br>- Returns **200 OK** with the order details or a **404 Not Found** if the ID does not exist. |
| **OM-06** | Implement `POST /fulfillment_orders/{order_id}/cancel` Endpoint | - The endpoint is created according to the OpenAPI specification.<br>- It loads the corresponding `FulfillmentOrder` aggregate.<br>- It calls the `cancel()` method on the aggregate, which enforces the business rule that an order cannot be cancelled if already shipped.<br>- On success, the updated aggregate state is persisted and a **202 Accepted** response is returned.<br>- Returns **400 Bad Request** if the cancellation is not allowed by the aggregate's state. |
| **OM-07** | Implement API Testing Suite | - Unit tests are written for the `FulfillmentOrder` aggregate's business logic.<br>- Integration tests are created for each API endpoint to verify correct behavior, including success paths, error handling (4xx codes), and idempotency. |

---

## Epic 2: Implement Asynchronous Order Event Publishing (Kafka)

**Goal:**  
To enable the service to publish domain events to a Kafka topic in the standard **CloudEvents** format, allowing for decoupled, choreographed workflows with other bounded contexts.

| Task ID | Task Description | Acceptance Criteria |
|---------|-----------------|----------------------|
| **OM-08** | Configure Kafka Producer & CloudEvents SDK | - The service is configured with the connection details for the Kafka broker.<br>- A Kafka producer is configured for reliability (e.g., with retries and acknowledgments).<br>- The official CloudEvents SDK is added as a dependency to standardize event creation. |
| **OM-09** | Implement Transactional Outbox Pattern | - A database table (**outbox**) is created to store events that need to be published.<br>- Logic is implemented to save the `FulfillmentOrder` aggregate and its corresponding domain event(s) to their respective tables within the same database transaction.<br>- A separate process/worker is created to read events from the outbox table and publish them to Kafka, ensuring at-least-once delivery. |
| **OM-10** | Implement `FulfillmentOrderReceived` Event Publishing | - When the `POST /fulfillment_orders` command succeeds, a `FulfillmentOrderReceived` event is created.<br>- The event is structured as a valid CloudEvent with type: `com.example.fulfillment.order.received`.<br>- The event is saved to the outbox table as part of the order creation transaction. |
| **OM-11** | Implement `FulfillmentOrderValidated` Event Publishing | - After an order is created, an internal validation process is triggered.<br>- Upon successful validation, a `FulfillmentOrderValidated` event is created.<br>- The event is structured as a valid CloudEvent with type: `com.example.fulfillment.order.validated`.<br>- The event is saved to the outbox table. |
| **OM-12** | Implement `FulfillmentOrderInvalidated` Event Publishing | - If the internal validation process fails, a `FulfillmentOrderInvalidated` event is created.<br>- The event is structured as a valid CloudEvent with type: `com.example.fulfillment.order.invalidated` and includes a reason in its data payload.<br>- The event is saved to the outbox table. |
| **OM-13** | Implement `FulfillmentOrderCancelled` Event Publishing | - When the `POST /fulfillment_orders/{order_id}/cancel` command succeeds, a `FulfillmentOrderCancelled` event is created.<br>- The event is structured as a valid CloudEvent with type: `com.example.fulfillment.order.cancelled`.<br>- The event is saved to the outbox table as part of the order cancellation transaction. |
| **OM-14** | Implement Event Publishing Test Suite | - Unit tests are written to verify the correct creation and structure of each CloudEvent.<br>- Integration tests are created to confirm that events are correctly written to the outbox table within the same transaction as the aggregate change.<br>- End-to-end tests are developed to verify that the outbox worker successfully publishes events to a test Kafka topic. |

---
