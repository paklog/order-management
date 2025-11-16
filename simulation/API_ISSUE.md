# Order Management API - Jackson Configuration Issue

## Problem Summary

The Order Management API at `http://localhost:8080` has a **Jackson deserialization configuration issue** that prevents it from accepting valid JSON requests.

## Configuration

In `application.yml`:
```yaml
spring:
  jackson:
    property-naming-strategy: SNAKE_CASE
```

This should make the API accept snake_case JSON for both input and output.

## Issue

**The `SNAKE_CASE` property naming strategy is NOT being applied to request deserialization (JSON → Java objects).**

### Evidence

1. **Postman Collection Format**: The included Postman collection (`fulfillment-order-management.postman_collection.json`) shows all fields in snake_case:
   ```json
   {
     "seller_fulfillment_order_id": "...",
     "destination_address": {
       "address_line_1": "...",
       "postal_code": "..."
     }
   }
   ```

2. **API Rejects Postman Format**: Testing with the exact Postman collection format results in:
   ```json
   {
     "field_errors": {
       "destinationAddress.addressLine1": ["Address line 1 is required"]
     }
   }
   ```

   Notice: Error shows `destinationAddress.addressLine1` (camelCase) even though we sent `address_line_1` (snake_case).

3. **API Also Rejects camelCase**: Testing with camelCase nested objects also fails with the same error.

## Root Cause

The Jackson `property-naming-strategy: SNAKE_CASE` configuration is likely:
- Only applied to **serialization** (Java → JSON output)
- **NOT applied to deserialization** (JSON → Java input)

This is a known issue in some Spring Boot/Jackson versions where the property naming strategy doesn't affect `@RequestBody` deserialization.

## Solution Required

The API needs one of these fixes:

### Option 1: Use `@JsonNaming` Annotation (Recommended)

Add to the DTO classes:
```java
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateFulfillmentOrderRequest {
    // ...
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Address {
    // ...
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderItem {
    // ...
}
```

### Option 2: Configure ObjectMapper Bean

```java
@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.propertyNamingStrategy(
            PropertyNamingStrategies.SNAKE_CASE);
    }
}
```

### Option 3: Update application.yml Syntax

For newer Spring Boot versions:
```yaml
spring:
  jackson:
    property-naming-strategy: SNAKE_CASE
    deserialization:
      accept-single-value-as-array: true
    mapper:
      accept-case-insensitive-properties: true
```

## Current K6 Script Status

The K6 load testing script (`simulation/scripts/order-load-test.js`) is correctly configured with:
- ✅ Top-level fields in snake_case
- ✅ Nested object fields in snake_case
- ✅ Dates set to yesterday (avoiding timezone issues)
- ✅ Valid product SKUs from catalog
- ✅ 2-9 items per order with no duplicates
- ✅ Rate of 2 orders/second

**The script is ready to run once the API is fixed.**

## Test Results

### Last Successful Configuration Test
- Date validation: ✅ PASSED (using yesterday's date)
- Request rate: ✅ 2.00 orders/second
- All other validations: ❌ FAILED due to Jackson deserialization issue

### Error Rate
- 100% of requests failing with: `"destinationAddress.addressLine1": ["Address line 1 is required"]`

## How to Verify the Fix

Once the API is fixed, run:
```bash
cd simulation/scripts
TEST_DURATION=10s BASE_URL=http://localhost:8080 k6 run order-load-test.js
```

Expected output:
```
Order Creation:
  Success Rate: >95%
  Avg Duration: <500ms
  P95 Duration: <2000ms
```

## Files

- API Configuration: `src/main/resources/application.yml`
- DTOs to fix:
  - `src/main/java/com/paklog/ordermanagement/interfaces/dto/CreateFulfillmentOrderRequest.java`
  - `src/main/java/com/paklog/ordermanagement/domain/model/Address.java`
  - `src/main/java/com/paklog/ordermanagement/domain/model/OrderItem.java`
- Postman Collection: `fulfillment-order-management.postman_collection.json`
- K6 Script: `simulation/scripts/order-load-test.js`

## Priority

**HIGH** - This blocks all API integrations that follow REST naming conventions with snake_case.
