# Order Management Load Testing Suite

Comprehensive load testing suite for the Order Management API using K6. This suite simulates realistic order creation scenarios by fetching valid products from the product catalog and generating orders with multiple items.

## Features

- Creates orders at a rate of 2 per second (1 every 0.5 seconds)
- Each order contains 2-9 randomly selected products
- Ensures no duplicate SKUs within the same order
- Uses real product data from the product catalog API
- Generates realistic shipping addresses and order details
- **Supports fulfillment policies** (FILL_OR_KILL, FILL_ALL, FILL_ALL_AVAILABLE)
- Simulates partial fulfillment scenarios
- Tracks comprehensive metrics and performance indicators
- Provides detailed success/failure reporting

## Prerequisites

### Required Software

1. **K6** - Load testing tool
   ```bash
   # macOS
   brew install k6

   # Linux (Debian/Ubuntu)
   sudo gpg -k
   sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
   echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
   sudo apt-get update
   sudo apt-get install k6

   # Windows
   choco install k6
   ```

2. **curl** - For API health checks (usually pre-installed)

3. **jq** - JSON processor (optional, for validation)
   ```bash
   # macOS
   brew install jq

   # Linux
   sudo apt-get install jq
   ```

### Running Services

Ensure the following services are running:

- **Order Management API** - Default: `http://localhost:8080`
- **Product Catalog API** - Default: `http://localhost:8081` (optional, will use sample data if not available)

## Directory Structure

```
simulation/
├── README.md                    # This file
├── config/
│   └── config.env              # Configuration file
├── data/
│   └── products.json           # Product catalog (generated)
├── scripts/
│   ├── fetch-products.sh       # Product catalog fetcher
│   ├── order-load-test.js      # K6 load test script
│   └── run-load-test.sh        # Main test runner
└── results/                    # Test results (generated)
    ├── output_*.log            # Console output
    ├── raw_*.json              # Raw K6 metrics
    └── summary_*.json          # Test summary
```

## Configuration

Edit `config/config.env` to customize the test parameters:

```bash
# API Endpoints
BASE_URL=http://localhost:8080
PRODUCT_CATALOG_URL=http://localhost:8081

# Load Test Parameters
ORDERS_PER_SECOND=2             # Orders per second (2 = 1 every 0.5s)
TEST_DURATION=5m                # Test duration (e.g., 30s, 5m, 1h)

# Virtual Users
MIN_VUS=5                       # Minimum virtual users
MAX_VUS=20                      # Maximum virtual users

# Order Configuration
MIN_ITEMS_PER_ORDER=2           # Minimum items per order
MAX_ITEMS_PER_ORDER=9           # Maximum items per order
```

## Quick Start

### Option 1: Run Complete Test Suite (Recommended)

The easiest way to run the load test:

```bash
cd simulation/scripts
./run-load-test.sh
```

This script will:
1. Fetch products from the catalog API (or create sample data)
2. Verify the Order Management API is healthy
3. Run the K6 load test
4. Save results to the `results/` directory

### Option 2: Manual Step-by-Step

#### Step 1: Fetch Product Catalog

```bash
cd simulation/scripts
./fetch-products.sh
```

This creates `simulation/data/products.json` with available products.

#### Step 2: Run K6 Load Test

```bash
cd simulation/scripts
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e PRODUCT_CATALOG_URL=http://localhost:8081 \
  order-load-test.js
```

## Load Test Scenarios

### Default Scenario: Constant Rate

The default configuration uses a constant-arrival-rate executor:

- **Rate**: 2 orders/second (1 every 0.5 seconds)
- **Duration**: 5 minutes
- **Virtual Users**: 5-20 (auto-scaled)
- **Items per Order**: 2-9 randomly selected products

### Custom Scenarios

You can modify the test script or pass environment variables:

#### Short Burst Test (30 seconds)

```bash
# Edit order-load-test.js and change duration to '30s'
# Or run with custom duration via script modification
```

#### High Load Test (10 orders/second)

Edit `simulation/scripts/order-load-test.js`:

```javascript
export const options = {
  scenarios: {
    constant_rate: {
      executor: 'constant-arrival-rate',
      rate: 10, // 10 orders per second
      timeUnit: '1s',
      duration: '2m',
      preAllocatedVUs: 20,
      maxVUs: 50,
    },
  },
};
```

#### Ramp-Up Test

Edit the scenarios section:

```javascript
export const options = {
  scenarios: {
    ramping_test: {
      executor: 'ramping-arrival-rate',
      startRate: 1,
      timeUnit: '1s',
      stages: [
        { duration: '1m', target: 2 },   // Ramp to 2/sec over 1 min
        { duration: '3m', target: 5 },   // Ramp to 5/sec over 3 min
        { duration: '1m', target: 2 },   // Ramp down to 2/sec
      ],
      preAllocatedVUs: 10,
      maxVUs: 50,
    },
  },
};
```

## Understanding Results

### Console Output

During the test, you'll see:

```
✓ Order created successfully: ORDER-1699564800000-1234 with 5 items (234ms)

====== Load Test Summary ======

Test Duration: 300s

HTTP Requests:
  Total: 600
  Rate: 2.00/s

Order Creation:
  Success Rate: 98.50%
  Avg Duration: 187.45ms
  P95 Duration: 345.67ms
  Max Duration: 892.12ms

Errors:
  Validation Errors: 3
  Duplicate SKU Errors: 0

================================
```

### Metrics Explained

- **Success Rate**: Percentage of orders created successfully (target: >95%)
- **Avg Duration**: Average time to create an order
- **P95 Duration**: 95th percentile duration (target: <2000ms)
- **Validation Errors**: Orders rejected due to validation failures
- **Duplicate SKU Errors**: Internal errors (should be 0)

### Response Validation

The test now also validates:
- Response includes `fulfillment_policy`
- Response includes `fulfillment_action`

### Result Files

All results are saved to `simulation/results/`:

- **output_TIMESTAMP.log**: Complete console output
- **raw_TIMESTAMP.json**: Raw K6 metrics in JSON format
- **summary_TIMESTAMP.json**: Test summary statistics

## Generated Order Structure

Each generated order includes:

### Order Details
- Unique seller fulfillment order ID
- Displayable order ID for customer reference
- Current timestamp as order date
- Random shipping speed category (STANDARD, EXPEDITED, PRIORITY, NEXT_DAY)
- Descriptive comment with item count

### Fulfillment Policy
Controls how the system handles inventory availability:

- **FILL_OR_KILL** (10% probability): Reject the entire order if any item is unavailable
- **FILL_ALL** (20% probability): Accept order, publish stock unavailable event when items are unavailable
- **FILL_ALL_AVAILABLE** (70% probability): Accept order, fulfill only available items (partial fulfillment)

The response will include:
- `fulfillment_policy`: The policy applied to the order
- `fulfillment_action`: Result of applying the policy (COMPLETE, PARTIAL, or UNFULFILLABLE)

**Note**: Detailed unfulfillable items information is communicated asynchronously via domain events, not in the synchronous API response.

### Shipping Address
- Random realistic US address from a pool of 8 addresses
- Includes name, street address, city, state, postal code, country code
- Properly formatted according to API requirements

### Order Items (2-9 items)
- Randomly selected products from the catalog
- **No duplicate SKUs** within the same order
- Random quantity (1-5) for each item
- Optional gift message (30% probability)
- Optional handling comment (20% probability)

### Headers
- Unique `Idempotency-Key` for each request
- Prevents duplicate order creation

## Troubleshooting

### Issue: "k6 is not installed"

**Solution**: Install K6 using the commands in the Prerequisites section.

### Issue: "Product Catalog API is not available"

**Solution**: The script will create sample product data automatically. You can:
1. Start the Product Catalog API at the configured URL
2. Use the sample data (30 products included)
3. Manually edit `simulation/data/products.json` to add more products

### Issue: "Order Management API may not be running"

**Solution**:
1. Start the Order Management API: `./mvnw spring-boot:run`
2. Verify it's accessible at `http://localhost:8080/actuator/health`

### Issue: High error rate

**Possible causes**:
1. API is overloaded - reduce the rate in configuration
2. Database connection issues - check MongoDB connection
3. Product validation failing - ensure product catalog is accessible
4. Network latency - adjust timeout thresholds

### Issue: "Duplicate SKU errors"

This should not happen as the script ensures unique SKUs per order. If you see this:
1. Check the product catalog has enough products (minimum 9 for max items)
2. Report as a bug in the script

## Advanced Usage

### Running with K6 Cloud

```bash
k6 cloud simulation/scripts/order-load-test.js
```

### Using K6 Web Dashboard

```bash
k6 run --out web-dashboard simulation/scripts/order-load-test.js
```

Then open the provided URL in your browser for real-time metrics.

### Custom Environment Variables

```bash
BASE_URL=http://api.example.com:8080 \
PRODUCT_CATALOG_URL=http://catalog.example.com:8081 \
k6 run simulation/scripts/order-load-test.js
```

### Exporting Results to Different Formats

```bash
# Export to InfluxDB
k6 run --out influxdb=http://localhost:8086/k6 order-load-test.js

# Export to Prometheus
k6 run --out experimental-prometheus-rw order-load-test.js

# Export to StatsD
k6 run --out statsd order-load-test.js
```

## Performance Thresholds

The test includes the following thresholds:

```javascript
thresholds: {
  'order_creation_success': ['rate>0.95'],     // 95% success rate
  'order_creation_duration': ['p(95)<2000'],   // 95% under 2 seconds
  'http_req_failed': ['rate<0.05'],            // <5% failed requests
}
```

If any threshold is violated, K6 will exit with a non-zero status code.

## Sample Product Data

If the Product Catalog API is not available, the script uses 30 sample products including:
- Laptops (Dell, MacBook, Lenovo)
- Phones (iPhone, Samsung, Google)
- Tablets (iPad, Samsung Tab)
- Watches (Apple, Samsung)
- Audio devices (Headphones, Speakers)
- Accessories (Keyboards, Mice, Cables, Cases)
- Office furniture (Desks, Chairs)

All products are properly formatted with SKU, name, price, active status, and category.

## Best Practices

1. **Start Small**: Begin with short duration tests (30s-1m) to verify setup
2. **Monitor Resources**: Watch CPU, memory, and database metrics during tests
3. **Gradual Increase**: Increase load gradually to find breaking points
4. **Baseline First**: Establish baseline performance before optimization
5. **Clean Data**: Clear test data between runs if needed
6. **Isolate Tests**: Run load tests in isolated environments when possible

## Integration with CI/CD

Add to your CI/CD pipeline:

```yaml
# Example GitHub Actions
- name: Run Load Test
  run: |
    cd simulation/scripts
    ./run-load-test.sh
  env:
    BASE_URL: ${{ secrets.API_URL }}
```

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review K6 documentation: https://k6.io/docs/
3. Check Order Management API logs for errors
4. Verify configuration in `config/config.env`

## License

This load testing suite is part of the Order Management project.
