import { randomItem, randomIntBetween, randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const products = JSON.parse(open('../data/products.json')).products;

console.log("Products loaded:", products.length);
console.log("First product:", JSON.stringify(products[0]));

const product = products[0];
const item = {
  sellerSku: product.sku,
  sellerFulfillmentOrderItemId: `item-1-${randomString(8)}`,
  quantity: randomIntBetween(1, 5),
};

console.log("Generated item:", JSON.stringify(item));

const orderPayload = {
  sellerFulfillmentOrderId: "TEST-123",
  displayableOrderId: "DISPLAY-123",
  displayableOrderDate: new Date().toISOString(),
  displayableOrderComment: "Test order",
  shippingSpeedCategory: "STANDARD",
  destinationAddress: {
    name: "John Doe",
    addressLine1: "123 Main St",
    city: "New York",
    stateOrRegion: "NY",
    postalCode: "10001",
    countryCode: "US",
  },
  items: [item],
  fulfillmentPolicy: "FILL_ALL_AVAILABLE",
};

console.log("Full payload:", JSON.stringify(orderPayload, null, 2));

export default function() {
  // Just exit
}
