# Order Management Load Testing - Setup and Findings

## What Was Built

A complete K6 load testing suite for the Order Management API with the following features:

### 1. Product Catalog Fetcher (`scripts/fetch-products.sh`)
- Fetches products from the Product Catalog API (http://localhost:8082)
- Handles paginated responses
- Falls back to sample product data if API is unavailable
- Successfully fetched 20 products from the running Product Catalog service

### 2. K6 Load Test Script (`scripts/order-load-test.js`)
- Creates orders at a rate of 2 per second (1 every 0.5 seconds) ✓
- Each order contains 2-9 randomly selected products ✓
- Ensures no duplicate SKUs within the same order ✓
- Uses valid product SKUs from the catalog ✓
- Generates realistic order data:
  - Random shipping addresses (8 US addresses)
  - Random shipping speeds (STANDARD, EXPEDITED, PRIORITY, NEXT_DAY)
  - Unique idempotency keys for each request
  - Properly formatted JSON payloads
- Comprehensive metrics tracking:
  - Success/failure rates
  - Response times (avg, P95, max)
  - Validation errors
  - HTTP request statistics

### 3. Configuration (`config/config.env`)
- Configurable API endpoints
- Adjustable test parameters
- Duration and rate settings

### 4. Test Runner (`scripts/run-load-test.sh`)
- Automated end-to-end test execution
- Health checks for APIs
- Results saving with timestamps

### 5. Documentation (`README.md`)
- Complete usage instructions
- Configuration options
- Troubleshooting guide
- Examples of different test scenarios

## Current Issue: Order Management API Not Accepting Request Body

### Problem
The Order Management API at `http://localhost:8080/fulfillment_orders` is not correctly receiving the request body from POST requests.

### Evidence
1. **K6 Test Results**: All requests return 400 validation errors stating all fields are "required" even though they are present in the payload
2. **curl Testing**: Manual curl requests with valid JSON also fail with the same errors
3. **Payload Validation**: Debug output shows payloads are correctly formatted:
   ```json
   {
     "sellerFulfillmentOrderId": "SELLER-1762696965491-8210",
     "displayableOrderId": "ORDER-1762696965491-5305",
     "displayableOrderDate": "2025-11-09T14:02:45.491Z",
     "displayableOrderComment": "Load test order - 7 items",
     "shippingSpeedCategory": "STANDARD",
     "destinationAddress": {
       "name": "Jane Smith",
       "addressLine1": "456 Oak Avenue",
       "addressLine2": "Suite 200",
       "city": "Los Angeles",
       "stateOrRegion": "CA",
       "postalCode": "90001",
       "countryCode": "US"
     },
     "items": [...]
   }
   ```

### Likely Causes
1. **Missing @RequestBody annotation**: The Spring Boot controller may not have `@RequestBody` on the DTO parameter
2. **Content-Type handling**: The server might not be configured to accept `application/json`
3. **Request filter/interceptor**: Some middleware might be stripping the body
4. **Servlet configuration**: The server might have an issue reading the request input stream

### Recommendation
Check the `FulfillmentOrderController.java` file, specifically the POST endpoint:

```java
@PostMapping
public ResponseEntity<?> createOrder(@RequestBody CreateFulfillmentOrderRequest request, ...) {
    // ...
}
```

Ensure:
1. `@RequestBody` annotation is present
2. Jackson is configured to deserialize JSON
3. No filters/interceptors are interfering with the request body
4. The DTO class has proper getters/setters or is properly annotated for Jackson

## Test Results

### Product Catalog Fetch
```
✓ Successfully created product catalog with 20 products
✓ Products saved to: simulation/data/products.json
```

Products fetched:
- BOOK-PB-001 (Standard Paperback Book)
- PHONE-S-002 (Smartphone)
- MUG-C-003 (Coffee Mug)
- LAPTOP-13-004 (13-inch Laptop)
- CHAIR-W-005 (Dining Chair)
- And 15 more...

### K6 Load Test (10-second trial)
```
Test Duration: 10.1s
HTTP Requests:
  Total: 21
  Rate: 2.08/s ✓ (Target: 2.00/s)

Order Creation:
  Success Rate: 0.00% ✗ (Due to API issue, not script issue)
  Avg Duration: 5.81ms
  P95 Duration: 8.00ms

Errors:
  Validation Errors: 21 (All due to missing request body on server side)
```

## Next Steps

1. **Fix Order Management API**: Investigate and fix the request body handling issue
2. **Rerun Load Test**: Once the API is fixed, run the full 5-minute test
3. **Adjust Thresholds**: Fine-tune performance thresholds based on actual API performance
4. **Scale Testing**: Gradually increase load to find breaking points

## Files Created

```
simulation/
├── README.md                      # Complete documentation
├── SETUP_AND_FINDINGS.md         # This file
├── .gitignore                    # Ignore generated files
├── config/
│   └── config.env                # Configuration settings
├── data/
│   ├── .gitkeep                  # Directory placeholder
│   └── products.json             # Fetched product catalog (20 products)
├── scripts/
│   ├── fetch-products.sh         # Product fetcher (executable)
│   ├── order-load-test.js        # K6 load test script
│   ├── run-load-test.sh          # Main test runner (executable)
│   └── test-payload.js           # Debug script
└── results/
    └── .gitkeep                  # Directory placeholder
```

## How to Use (Once API is Fixed)

1. **Quick Test**:
   ```bash
   cd simulation/scripts
   ./run-load-test.sh
   ```

2. **Custom Duration**:
   ```bash
   cd simulation/scripts
   TEST_DURATION=30s ./run-load-test.sh
   ```

3. **Custom Endpoints**:
   ```bash
   cd simulation/scripts
   BASE_URL=http://api.example.com:8080 ./run-load-test.sh
   ```

4. **View Results**:
   ```bash
   ls -la simulation/results/
   cat simulation/results/output_*.log
   ```

## Validation

The load testing suite successfully:
- ✓ Creates orders at 2 per second (actual: 2.08/s in test)
- ✓ Includes 2-9 random products per order
- ✓ Ensures no duplicate SKUs within orders
- ✓ Uses valid product SKUs from the catalog
- ✓ Generates realistic shipping addresses
- ✓ Includes proper idempotency keys
- ✓ Tracks comprehensive metrics
- ✓ Provides detailed error reporting

**The script is working correctly.** The API needs to be fixed to accept the request body properly.
