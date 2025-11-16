#!/bin/bash

# Product Catalog Fetcher Script
# This script fetches all active products from the product catalog API
# and stores them in a JSON file for use by the k6 load testing script

set -e

# Load configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/../config/config.env"

if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
else
    echo "Configuration file not found at $CONFIG_FILE"
    echo "Using default values..."
    PRODUCT_CATALOG_URL="${PRODUCT_CATALOG_URL:-http://localhost:8081}"
fi

OUTPUT_FILE="${SCRIPT_DIR}/../data/products.json"

echo "Fetching products from Product Catalog API: $PRODUCT_CATALOG_URL"

# Function to create sample product data
function create_sample_products() {
    # Create sample product data
    cat > "$OUTPUT_FILE" << 'EOF'
{
  "products": [
    {"sku": "LAPTOP-001", "name": "Dell XPS 15 Laptop", "price": 1299.99, "active": true, "category": "Electronics"},
    {"sku": "LAPTOP-002", "name": "MacBook Pro 16", "price": 2499.99, "active": true, "category": "Electronics"},
    {"sku": "LAPTOP-003", "name": "Lenovo ThinkPad X1", "price": 1599.99, "active": true, "category": "Electronics"},
    {"sku": "PHONE-001", "name": "iPhone 15 Pro", "price": 999.99, "active": true, "category": "Electronics"},
    {"sku": "PHONE-002", "name": "Samsung Galaxy S24", "price": 899.99, "active": true, "category": "Electronics"},
    {"sku": "PHONE-003", "name": "Google Pixel 8", "price": 699.99, "active": true, "category": "Electronics"},
    {"sku": "TABLET-001", "name": "iPad Pro 12.9", "price": 1099.99, "active": true, "category": "Electronics"},
    {"sku": "TABLET-002", "name": "Samsung Tab S9", "price": 799.99, "active": true, "category": "Electronics"},
    {"sku": "WATCH-001", "name": "Apple Watch Ultra", "price": 799.99, "active": true, "category": "Wearables"},
    {"sku": "WATCH-002", "name": "Samsung Galaxy Watch", "price": 399.99, "active": true, "category": "Wearables"},
    {"sku": "HEADPHONE-001", "name": "Sony WH-1000XM5", "price": 399.99, "active": true, "category": "Audio"},
    {"sku": "HEADPHONE-002", "name": "Bose QuietComfort", "price": 349.99, "active": true, "category": "Audio"},
    {"sku": "HEADPHONE-003", "name": "AirPods Pro", "price": 249.99, "active": true, "category": "Audio"},
    {"sku": "SPEAKER-001", "name": "Sonos One", "price": 219.99, "active": true, "category": "Audio"},
    {"sku": "SPEAKER-002", "name": "JBL Flip 6", "price": 129.99, "active": true, "category": "Audio"},
    {"sku": "KEYBOARD-001", "name": "Logitech MX Keys", "price": 99.99, "active": true, "category": "Accessories"},
    {"sku": "MOUSE-001", "name": "Logitech MX Master 3", "price": 99.99, "active": true, "category": "Accessories"},
    {"sku": "MONITOR-001", "name": "Dell UltraSharp 27", "price": 499.99, "active": true, "category": "Electronics"},
    {"sku": "MONITOR-002", "name": "LG UltraWide 34", "price": 699.99, "active": true, "category": "Electronics"},
    {"sku": "CAMERA-001", "name": "Canon EOS R6", "price": 2499.99, "active": true, "category": "Photography"},
    {"sku": "CAMERA-002", "name": "Sony A7 IV", "price": 2499.99, "active": true, "category": "Photography"},
    {"sku": "DRONE-001", "name": "DJI Mini 3 Pro", "price": 759.99, "active": true, "category": "Photography"},
    {"sku": "CHARGER-001", "name": "Anker PowerPort", "price": 29.99, "active": true, "category": "Accessories"},
    {"sku": "CABLE-001", "name": "USB-C Cable 6ft", "price": 19.99, "active": true, "category": "Accessories"},
    {"sku": "CASE-001", "name": "Laptop Sleeve 15", "price": 39.99, "active": true, "category": "Accessories"},
    {"sku": "BACKPACK-001", "name": "Tech Backpack", "price": 89.99, "active": true, "category": "Accessories"},
    {"sku": "DESK-001", "name": "Standing Desk", "price": 599.99, "active": true, "category": "Furniture"},
    {"sku": "CHAIR-001", "name": "Ergonomic Office Chair", "price": 399.99, "active": true, "category": "Furniture"},
    {"sku": "LAMP-001", "name": "LED Desk Lamp", "price": 49.99, "active": true, "category": "Accessories"},
    {"sku": "WEBCAM-001", "name": "Logitech Brio 4K", "price": 199.99, "active": true, "category": "Electronics"}
  ]
}
EOF
}

# Check if the product catalog API is available
if curl -f -s -o /dev/null "$PRODUCT_CATALOG_URL/actuator/health" 2>/dev/null; then
    echo "Product Catalog API is available"

    # Fetch all products from paginated API
    ALL_PRODUCTS="[]"
    PAGE=0
    HAS_MORE=true

    while [ "$HAS_MORE" = true ]; do
        RESPONSE=$(curl -s "$PRODUCT_CATALOG_URL/products?page=$PAGE&size=100" -H "Accept: application/json" 2>/dev/null || echo "")

        if [ -n "$RESPONSE" ] && command -v jq &> /dev/null; then
            # Extract products from the content array
            PAGE_PRODUCTS=$(echo "$RESPONSE" | jq '.content // []')
            ALL_PRODUCTS=$(echo "$ALL_PRODUCTS" "$PAGE_PRODUCTS" | jq -s 'add')

            # Check if there are more pages
            IS_LAST=$(echo "$RESPONSE" | jq -r '.is_last // true')
            if [ "$IS_LAST" = "true" ]; then
                HAS_MORE=false
            else
                PAGE=$((PAGE + 1))
            fi
        else
            echo "Could not fetch page $PAGE or jq not available"
            HAS_MORE=false
            if [ "$PAGE" -eq 0 ]; then
                echo "Creating sample product data..."
                create_sample_products
                exit 0
            fi
        fi
    done

    # Transform products to simpler format with required fields
    if command -v jq &> /dev/null; then
        echo "$ALL_PRODUCTS" | jq '{products: [.[] | {sku: .sku, name: .title, price: 99.99, active: true, category: "General"}]}' > "$OUTPUT_FILE"
        echo "Products fetched successfully from API"
    else
        create_sample_products
    fi
else
    echo "Product Catalog API is not available at $PRODUCT_CATALOG_URL"
    echo "Creating sample product data for testing..."
    create_sample_products
fi

# Validate the JSON file
if command -v jq &> /dev/null; then
    if jq empty "$OUTPUT_FILE" 2>/dev/null; then
        PRODUCT_COUNT=$(jq '.products | length' "$OUTPUT_FILE")
        echo "✓ Successfully created product catalog with $PRODUCT_COUNT products"
        echo "✓ Products saved to: $OUTPUT_FILE"
    else
        echo "✗ Error: Invalid JSON in output file"
        exit 1
    fi
else
    echo "✓ Products saved to: $OUTPUT_FILE"
    echo "  (Install jq for JSON validation)"
fi
