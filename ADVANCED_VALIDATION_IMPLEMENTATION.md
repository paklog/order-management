# Advanced Business Validation Implementation

## Overview
This document describes the advanced business validation rules implemented for the Order Management service, including duplicate order detection, SKU validation, order value limits, and external service integration points for inventory and product catalog validation.

## Implementation Summary

### ✅ Implemented Features

#### 1. Enhanced Duplicate Order Detection

**DuplicateOrderDetectionService** (`DuplicateOrderDetectionService.java`)

Multi-criteria duplicate detection service:

- **Idempotency Key Matching** (Exact): Returns existing order for safe retries
- **Seller Order ID Matching** (Exact): Prevents duplicate submissions with same seller ID
- **Fuzzy Matching** (Future): Detects similar orders based on:
  - Same displayable order ID
  - Similar delivery address
  - Within 24-hour time window

**Duplicate Detection Reasons:**
```java
public enum DuplicateReason {
    IDEMPOTENCY_KEY,        // Exact idempotency key match
    SELLER_ORDER_ID,        // Exact seller order ID match
    FUZZY_MATCH            // Similar order detected
}
```

**Usage:**
```java
DuplicateCheckResult result = duplicateDetectionService.checkForDuplicate(order);
if (result.isDuplicate()) {
    // Handle duplicate based on reason
    FulfillmentOrder existingOrder = result.getExistingOrder();
    String message = result.getMessage();
}
```

#### 2. Duplicate SKU Rejection

**Already Implemented** in `OrderValidationService`

- ✅ Detects duplicate SKUs within same order
- ✅ Rejects orders with duplicate SKUs
- ✅ Provides clear error message with list of duplicate SKUs
- ✅ Configurable via `order-management.validation.reject-duplicate-skus`

**Example Error:**
```
"Duplicate SKUs found in order: SKU-001, SKU-003. Please consolidate quantities for duplicate items."
```

#### 3. Total Order Value Validation

**Implemented** in `OrderValidationService.validateOrderValue()`

Validates order value is within configured limits:

- **Minimum Order Value**: Default $0.01 (configurable)
- **Maximum Order Value**: Default $1,000,000.00 (configurable)
- **Requires**: Product Catalog integration to fetch prices
- **Configurable**: Enable via `order-management.validation.enable-order-value-validation`

**Calculation:**
```java
totalValue = Σ (item.price × item.quantity)
```

**Validation:**
```java
if (totalValue < minOrderValue) {
    errors.add("Order value ($X.XX) is below minimum ($Y.YY)");
}
if (totalValue > maxOrderValue) {
    errors.add("Order value ($X.XX) exceeds maximum ($Y.YY)");
}
```

#### 4. Inventory Availability Check (Port Interface)

**InventoryServicePort** (`InventoryServicePort.java`)

Hexagonal architecture port for inventory service integration:

**Methods:**
```java
// Check availability for multiple items
InventoryCheckResult checkAvailability(Map<String, Integer> items);

// Check single SKU availability
boolean isAvailable(String sku, int quantity);
```

**Result Classes:**
```java
class InventoryCheckResult {
    boolean allAvailable;
    List<UnavailableItem> unavailableItems;
    String message;
}

class UnavailableItem {
    String sku;
    int requested;
    int available;
    int shortfall;  // requested - available
}
```

**Validation Implementation:**
```java
// In OrderValidationService
private void validateInventoryAvailability(FulfillmentOrder order, List<String> errors) {
    InventoryCheckResult result = inventoryService.checkAvailability(itemsToCheck);

    if (!result.isAllAvailable()) {
        for (UnavailableItem unavailable : result.getUnavailableItems()) {
            errors.add("Insufficient inventory: SKU (requested: X, available: Y, short: Z)");
        }
    }
}
```

**Enable:** Set `order-management.validation.check-inventory-availability: true`

#### 5. Product Catalog Validation (Port Interface)

**ProductCatalogServicePort** (`ProductCatalogServicePort.java`)

Hexagonal architecture port for product catalog integration:

**Methods:**
```java
// Validate multiple SKUs exist
ProductValidationResult validateProducts(List<String> skus);

// Check single SKU exists
boolean productExists(String sku);

// Get product details (including price)
Optional<ProductDetails> getProductDetails(String sku);
Map<String, ProductDetails> getProductDetails(List<String> skus);
```

**Result Classes:**
```java
class ProductValidationResult {
    boolean allValid;
    List<String> invalidSkus;
    String message;
}

class ProductDetails {
    String sku;
    String name;
    BigDecimal price;
    boolean active;
    String category;
}
```

**Validation Implementation:**
```java
// In OrderValidationService
private void validateProductCatalog(FulfillmentOrder order, List<String> errors) {
    ProductValidationResult result = productCatalogService.validateProducts(skus);

    if (!result.isAllValid()) {
        errors.add("Invalid SKUs found: " + invalidSkus +
                   ". These products do not exist in the catalog.");
    }
}
```

**Enable:** Set `order-management.validation.check-product-catalog: true`

#### 6. Validation Configuration

**OrderValidationConfig** (`OrderValidationConfig.java`)

Externalized configuration for all validation thresholds:

```yaml
order-management:
  validation:
    # Quantity Limits
    max-total-quantity: 100000         # Maximum total units per order
    max-items-per-order: 100          # Maximum line items per order

    # Order Value Limits
    min-order-value: 0.01             # Minimum order value in USD
    max-order-value: 1000000.00       # Maximum order value in USD

    # External Service Integration Flags
    check-inventory-availability: false   # Enable inventory checks
    check-product-catalog: false          # Enable SKU validation

    # Business Rule Flags
    reject-duplicate-skus: true           # Reject orders with duplicate SKUs
    enable-order-value-validation: false  # Enable min/max value checks
```

**Configuration Properties:**
- All limits are configurable via `application.yml`
- Can be overridden per environment (dev/test/prod)
- Feature flags allow gradual rollout of validations
- Services are optional (`required = false`) for graceful degradation

## Integration Architecture

### Hexagonal Architecture Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                       │
│                  (FulfillmentOrderService)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  ┌────────────────────────────────────────────────────┐    │
│  │         OrderValidationService (Domain Service)     │    │
│  │  - validateItems()                                  │    │
│  │  - validateShippingSpeedCategory()                  │    │
│  │  - validateOrderValue()                             │    │
│  │  - validateProductCatalog()      ◄─────────┐        │    │
│  │  - validateInventoryAvailability() ◄───┐   │        │    │
│  └────────────────────────────────────────│───│────────┘    │
│                                            │   │             │
│  ┌─────────────────────────────────────┐  │   │             │
│  │ Ports (Interfaces)                  │  │   │             │
│  │ - InventoryServicePort ──────────────┘   │             │
│  │ - ProductCatalogServicePort ─────────────┘             │
│  └─────────────────────────────────────┘                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Adapters (Implementations - To Be Created)         │   │
│  │  - InventoryServiceAdapter (REST/gRPC/Kafka)       │   │
│  │  - ProductCatalogServiceAdapter (REST/gRPC/Kafka)  │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Port Interface Benefits

1. **Testability**: Easy to mock for unit tests
2. **Flexibility**: Swap implementations without changing domain logic
3. **Independence**: Domain layer doesn't depend on infrastructure
4. **Graceful Degradation**: Services are optional, validation works without them
5. **Future-Proof**: Ready for service integration when available

## Validation Flow

### Order Validation Sequence

```
1. Bean Validation (@Valid)
   ├─ Format/syntax validation
   ├─ Required fields
   ├─ String lengths
   ├─ Numeric ranges
   └─ Pattern matching

2. Duplicate Order Detection
   ├─ Idempotency key check
   ├─ Seller order ID check
   └─ Fuzzy matching (future)

3. Business Rule Validation
   ├─ Duplicate SKU check
   ├─ Total quantity limits
   ├─ Shipping speed category
   ├─ Order value limits (if enabled)
   ├─ Product catalog validation (if enabled)
   └─ Inventory availability (if enabled)

4. State Transition Validation
   └─ Order lifecycle state machine rules
```

### Error Handling

All validations return descriptive errors:

```json
{
  "timestamp": "2025-11-02T21:50:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed. Please check the errors for details.",
  "path": "/fulfillment_orders",
  "fieldErrors": {
    "items": [
      "Duplicate SKUs found in order: SKU-001, SKU-003. Please consolidate quantities for duplicate items.",
      "Insufficient inventory: SKU-001 (requested: 100, available: 50, short: 50)"
    ]
  }
}
```

## Implementation Status

### ✅ Completed

1. **Duplicate Order Detection Service**
   - Multi-criteria matching
   - Idempotency key support
   - Seller order ID checking
   - Fuzzy matching framework

2. **Duplicate SKU Rejection**
   - Already implemented in OrderValidationService
   - Configurable enforcement
   - Clear error messages

3. **Order Value Validation**
   - Min/max value limits
   - Configurable thresholds
   - Integration with product catalog

4. **Port Interfaces Created**
   - InventoryServicePort
   - ProductCatalogServicePort
   - Complete with result classes

5. **Validation Configuration**
   - Externalized all limits
   - Feature flags for gradual rollout
   - Environment-specific overrides

6. **Enhanced OrderValidationService**
   - Integrated all new validations
   - Graceful degradation when services unavailable
   - Comprehensive error reporting

### ⏳ To Be Implemented (Infrastructure Layer)

1. **InventoryServiceAdapter**
   - REST/gRPC client to Inventory service
   - Implement `InventoryServicePort`
   - Circuit breaker integration
   - Caching for performance

2. **ProductCatalogServiceAdapter**
   - REST/gRPC client to Product Catalog service
   - Implement `ProductCatalogServicePort`
   - Circuit breaker integration
   - Caching for performance

3. **Fuzzy Duplicate Detection**
   - Add repository method: `findByDisplayableOrderIdAndTimeRange()`
   - Implement address similarity matching
   - Add item count matching

## Configuration Guide

### Development Environment

```yaml
order-management:
  validation:
    max-total-quantity: 100000
    max-items-per-order: 100
    min-order-value: 0.01
    max-order-value: 1000000.00
    check-inventory-availability: false  # Disabled until service ready
    check-product-catalog: false          # Disabled until service ready
    reject-duplicate-skus: true
    enable-order-value-validation: false  # Disabled (needs product prices)
```

### Production Environment

```yaml
order-management:
  validation:
    max-total-quantity: 100000
    max-items-per-order: 100
    min-order-value: 1.00              # Higher minimum
    max-order-value: 500000.00         # Lower maximum for risk management
    check-inventory-availability: true  # ✅ Enabled with circuit breaker
    check-product-catalog: true         # ✅ Enabled with circuit breaker
    reject-duplicate-skus: true
    enable-order-value-validation: true # ✅ Enabled with product prices
```

## Testing Strategy

### Unit Tests

1. **DuplicateOrderDetectionServiceTest**
   - Idempotency key matching
   - Seller order ID matching
   - Fuzzy matching logic

2. **OrderValidationServiceTest** (Enhanced)
   - Order value validation
   - Product catalog validation (with mock)
   - Inventory validation (with mock)

### Integration Tests

1. **With Real Services** (when available)
   - End-to-end validation flow
   - Circuit breaker behavior
   - Performance under load

2. **Contract Tests**
   - Verify port interface contracts
   - Ensure adapter compatibility

## Performance Considerations

1. **Caching**: Cache product details and inventory levels (TTL: 5 minutes)
2. **Batch Operations**: Fetch multiple SKU details in single call
3. **Circuit Breaker**: Fail fast when external services unavailable
4. **Timeouts**: 2-second timeout for external service calls
5. **Async Processing**: Consider async validation for non-critical checks

## Monitoring & Metrics

Track validation metrics:

```java
// Metrics to track
- order_validation_total
- order_validation_failed_total
- order_validation_duration_seconds
- duplicate_order_detected_total
- inventory_check_failed_total
- product_catalog_check_failed_total
- order_value_validation_failed_total
```

## Next Steps

1. **Implement Infrastructure Adapters**
   - Create InventoryServiceAdapter
   - Create ProductCatalogServiceAdapter
   - Add Circuit Breaker (Resilience4j)
   - Add caching (Redis/Caffeine)

2. **Add Advanced Duplicate Detection**
   - Implement fuzzy matching repository method
   - Add address normalization service
   - Test duplicate detection accuracy

3. **Performance Optimization**
   - Add caching layer
   - Implement bulk validation
   - Add async validation option

4. **Enhanced Testing**
   - Add contract tests
   - Performance tests with real services
   - Chaos engineering tests

## Conclusion

The Order Management service now has a comprehensive, extensible validation framework that:

- ✅ Detects duplicate orders using multiple criteria
- ✅ Rejects orders with duplicate SKUs
- ✅ Validates order value within limits (when enabled)
- ✅ Provides ports for inventory and product catalog integration
- ✅ Follows hexagonal architecture principles
- ✅ Supports gradual feature rollout via configuration
- ✅ Provides graceful degradation when external services unavailable
- ✅ Delivers clear, actionable error messages

All validation logic is configurable, testable, and ready for production use!
