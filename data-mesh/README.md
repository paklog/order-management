# Data Product: Fulfillment Orders

## 1. Overview

This document outlines the architecture for the "Fulfillment Orders" data product, owned and served by the Order Management domain team.

The goal is to provide a high-quality, near real-time, and self-service analytical view of fulfillment orders. This enables other domains (e.g., Analytics, Logistics, Finance) to build reports, dashboards, and new services without coupling to our internal operational database.

**Owner:** `order-management-team`

## 2. Architecture

We will follow a non-invasive Change Data Capture (CDC) approach that decouples our operational database from the analytical serving layer without requiring any application code changes.

**Data Flow:**
`MongoDB (Outbox Collection)` -> `Debezium (CDC)` -> `Apache Kafka` -> `Apache Pinot (Analytical Serving)` -> `Data Consumers`

1.  **Change Data Capture (CDC):** A Debezium connector will be configured to monitor the `outboxEvent` collection in the operational MongoDB database. Debezium captures every insert, update, and delete operation as a structured event.
2.  **Streaming Backbone (Kafka):** Debezium publishes these change events to a dedicated Kafka topic, e.g., `prod.order-management.fulfillment-orders.v1`. This provides a durable, scalable buffer of all data changes.
3.  **Analytical Serving (Pinot):** An Apache Pinot table will be configured to ingest events from the Kafka topic in near real-time. Pinot will serve as the low-latency OLAP engine, allowing consumers to run complex analytical queries over the order data.
4.  **Consumption:** Data consumers will access the data product by querying Pinot's SQL endpoint. Access details and schema are formally defined in the `datacontract.yaml`.

## 3. Governance and Discovery

-   **Formal Contract:** The data product is defined by the `datacontract.yaml` file. This contract is the single source of truth for schema, quality guarantees (SLOs), and access information.
-   **Discoverability:** The data contract will be published to a central data catalog, allowing other teams to discover and understand this data product.
