import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { randomItem, randomIntBetween, randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// Load product catalog
let products;
try {
  const productsData = JSON.parse(open('../data/products.json'));
  products = productsData.products || productsData;
} catch (e) {
  console.error('Failed to load products.json. Please run fetch-products.sh first!');
  throw new Error('Products catalog not found. Run: ./fetch-products.sh');
}

// Custom metrics
const orderCreationRate = new Rate('order_creation_success');
const orderCreationDuration = new Trend('order_creation_duration');
const duplicateSkuErrors = new Counter('duplicate_sku_errors');
const validationErrors = new Counter('validation_errors');

// Test configuration
export const options = {
  scenarios: {
    constant_rate: {
      executor: 'constant-arrival-rate',
      rate: 2, // 2 iterations per second (1 every 0.5s)
      timeUnit: '1s',
      duration: __ENV.TEST_DURATION || '5m', // Run for 5 minutes (or custom duration)
      preAllocatedVUs: 5, // Pre-allocate 5 VUs
      maxVUs: 20, // Maximum 20 VUs if needed
    },
  },
  thresholds: {
    'order_creation_success': ['rate>0.95'], // 95% success rate
    'order_creation_duration': ['p(95)<2000'], // 95% of requests under 2s
    'http_req_failed': ['rate<0.05'], // Less than 5% failed requests
  },
};

// Configuration - can be overridden by environment variables
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PRODUCT_CATALOG_URL = __ENV.PRODUCT_CATALOG_URL || 'http://localhost:8081';

// Helper functions
function generateIdempotencyKey() {
  const timestamp = Date.now();
  const random = randomString(16, 'abcdefghijklmnopqrstuvwxyz0123456789');
  return `order-${timestamp}-${random}`;
}

function generateOrderId(prefix = 'SELLER') {
  const timestamp = Date.now();
  const random = randomIntBetween(1000, 9999);
  return `${prefix}-${timestamp}-${random}`;
}

function getRandomProducts(count) {
  // Shuffle products and take unique items
  const shuffled = [...products].sort(() => 0.5 - Math.random());
  return shuffled.slice(0, count);
}

function generateAddress() {
  const addresses = [
    {
      name: 'John Doe',
      address_line_1: '123 Main Street',
      address_line_2: 'Apt 4B',
      city: 'New York',
      state_or_region: 'NY',
      postal_code: '10001',
      country_code: 'US',
    },
    {
      name: 'Jane Smith',
      address_line_1: '456 Oak Avenue',
      address_line_2: 'Suite 200',
      city: 'Los Angeles',
      state_or_region: 'CA',
      postal_code: '90001',
      country_code: 'US',
    },
    {
      name: 'Robert Johnson',
      address_line_1: '789 Pine Road',
      address_line_2: '',
      city: 'Chicago',
      state_or_region: 'IL',
      postal_code: '60601',
      country_code: 'US',
    },
    {
      name: 'Maria Garcia',
      address_line_1: '321 Elm Boulevard',
      address_line_2: 'Floor 3',
      city: 'Houston',
      state_or_region: 'TX',
      postal_code: '77001',
      country_code: 'US',
    },
    {
      name: 'Michael Brown',
      address_line_1: '654 Maple Drive',
      address_line_2: '',
      city: 'Phoenix',
      state_or_region: 'AZ',
      postal_code: '85001',
      country_code: 'US',
    },
    {
      name: 'Lisa Anderson',
      address_line_1: '987 Cedar Lane',
      address_line_2: 'Unit 12',
      city: 'Philadelphia',
      state_or_region: 'PA',
      postal_code: '19019',
      country_code: 'US',
    },
    {
      name: 'David Martinez',
      address_line_1: '147 Birch Street',
      address_line_2: '',
      city: 'San Antonio',
      state_or_region: 'TX',
      postal_code: '78201',
      country_code: 'US',
    },
    {
      name: 'Sarah Wilson',
      address_line_1: '258 Spruce Court',
      address_line_2: 'Building A',
      city: 'San Diego',
      state_or_region: 'CA',
      postal_code: '92101',
      country_code: 'US',
    },
  ];

  return randomItem(addresses);
}

function generateShippingSpeed() {
  const speeds = ['STANDARD', 'EXPEDITED', 'PRIORITY', 'NEXT_DAY'];
  return randomItem(speeds);
}

function generateFulfillmentPolicy() {
  const policies = ['FILL_OR_KILL', 'FILL_ALL', 'FILL_ALL_AVAILABLE'];
  // Weighted selection: 70% FILL_ALL_AVAILABLE, 20% FILL_ALL, 10% FILL_OR_KILL
  const rand = Math.random();
  if (rand < 0.1) {
    return 'FILL_OR_KILL';
  } else if (rand < 0.3) {
    return 'FILL_ALL';
  } else {
    return 'FILL_ALL_AVAILABLE';
  }
}

function generateOrderItems(productCount) {
  const selectedProducts = getRandomProducts(productCount);
  const items = [];
  const usedSkus = new Set();

  for (let i = 0; i < selectedProducts.length; i++) {
    const product = selectedProducts[i];

    // Ensure no duplicate SKUs
    if (usedSkus.has(product.sku)) {
      duplicateSkuErrors.add(1);
      console.error(`Duplicate SKU detected: ${product.sku}`);
      continue;
    }

    usedSkus.add(product.sku);

    const item = {
      seller_sku: product.sku,
      seller_fulfillment_order_item_id: `item-${i + 1}-${randomString(8)}`,
      quantity: randomIntBetween(1, 5),
    };

    // Add optional fields only if they have values
    if (Math.random() > 0.7) {
      item.gift_message = 'Thank you for your purchase!';
    }
    if (Math.random() > 0.8) {
      item.displayable_comment = 'Handle with care';
    }

    items.push(item);
  }

  return items;
}

function createOrder() {
  const idempotencyKey = generateIdempotencyKey();
  const sellerOrderId = generateOrderId('SELLER');
  const displayableOrderId = generateOrderId('ORDER');
  const itemCount = randomIntBetween(2, 9); // 2 to 9 items per order

  // Use yesterday's date to avoid timezone issues with @PastOrPresent validation
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  const orderDate = yesterday.toISOString();

  const fulfillmentPolicy = generateFulfillmentPolicy();
  const orderPayload = {
    seller_fulfillment_order_id: sellerOrderId,
    displayable_order_id: displayableOrderId,
    displayable_order_date: orderDate,
    displayable_order_comment: `Load test order - ${itemCount} items`,
    shipping_speed_category: generateShippingSpeed(),
    destination_address: generateAddress(),
    items: generateOrderItems(itemCount),
    fulfillment_policy: fulfillmentPolicy,
  };

  const headers = {
    'Content-Type': 'application/json',
    'Idempotency-Key': idempotencyKey,
  };

  const startTime = Date.now();
  const body = JSON.stringify(orderPayload);

  // Debug first request only
  if (__ITER === 0) {
    console.log('Sample order payload:', body.substring(0, 500));
  }

  const response = http.post(
    `${BASE_URL}/fulfillment_orders`,
    body,
    { headers }
  );
  const duration = Date.now() - startTime;

  // Record metrics
  orderCreationDuration.add(duration);

  // Validation checks
  const success = check(response, {
    'status is 202': (r) => r.status === 202,
    'response has orderId': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.order_id !== undefined && body.order_id !== null;
      } catch (e) {
        return false;
      }
    },
    'response has correct status': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.status === 'NEW' || body.status === 'RECEIVED';
      } catch (e) {
        return false;
      }
    },
    'items count matches request': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.items && body.items.length === orderPayload.items.length;
      } catch (e) {
        return false;
      }
    },
    'response has fulfillment_policy': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.fulfillment_policy !== undefined;
      } catch (e) {
        return false;
      }
    },
    'response has fulfillment_action': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.fulfillment_action !== undefined;
      } catch (e) {
        return false;
      }
    },
  });

  orderCreationRate.add(success);

  // Log errors
  if (!success) {
    if (response.status === 400) {
      validationErrors.add(1);
      console.error(`Validation error: ${response.body}`);
    } else if (response.status === 409) {
      console.error(`Duplicate order detected: ${idempotencyKey}`);
    } else {
      console.error(
        `Order creation failed: Status ${response.status}, Body: ${response.body}`
      );
    }
  }

  // Log success details periodically
  if (success && __ITER % 50 === 0) {
    console.log(
      `✓ Order created successfully: ${displayableOrderId} with ${itemCount} items (${duration}ms)`
    );
  }

  return response;
}

export default function () {
  createOrder();

  // Small sleep to prevent overwhelming the system
  // The constant-arrival-rate executor will handle the rate limiting
  sleep(0.1);
}

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'summary.json': JSON.stringify(data),
  };
}

function textSummary(data, options) {
  const indent = options.indent || '';
  const enableColors = options.enableColors || false;

  let summary = '\n' + indent + '====== Load Test Summary ======\n\n';

  // Test duration
  summary += indent + `Test Duration: ${data.state.testRunDurationMs / 1000}s\n\n`;

  // HTTP metrics
  if (data.metrics.http_reqs) {
    summary += indent + 'HTTP Requests:\n';
    summary += indent + `  Total: ${data.metrics.http_reqs.values.count}\n`;
    summary += indent + `  Rate: ${data.metrics.http_reqs.values.rate.toFixed(2)}/s\n\n`;
  }

  // Order creation metrics
  if (data.metrics.order_creation_success) {
    const successRate = data.metrics.order_creation_success.values.rate * 100;
    summary += indent + 'Order Creation:\n';
    summary += indent + `  Success Rate: ${successRate.toFixed(2)}%\n`;
  }

  if (data.metrics.order_creation_duration) {
    summary += indent + `  Avg Duration: ${data.metrics.order_creation_duration.values.avg.toFixed(2)}ms\n`;
    summary += indent + `  P95 Duration: ${data.metrics.order_creation_duration.values['p(95)'].toFixed(2)}ms\n`;
    summary += indent + `  Max Duration: ${data.metrics.order_creation_duration.values.max.toFixed(2)}ms\n\n`;
  }

  // Error counts
  if (data.metrics.validation_errors) {
    summary += indent + 'Errors:\n';
    summary += indent + `  Validation Errors: ${data.metrics.validation_errors.values.count}\n`;
  }
  if (data.metrics.duplicate_sku_errors) {
    summary += indent + `  Duplicate SKU Errors: ${data.metrics.duplicate_sku_errors.values.count}\n`;
  }

  summary += '\n' + indent + '================================\n';

  return summary;
}
