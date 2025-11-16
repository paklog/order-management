#!/bin/bash

# Order Management Load Test Runner
# This script orchestrates the complete load testing process

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/../config/config.env"
RESULTS_DIR="${SCRIPT_DIR}/../results"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Load configuration
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
    echo -e "${GREEN}✓ Configuration loaded from $CONFIG_FILE${NC}"
else
    echo -e "${YELLOW}⚠ Configuration file not found, using defaults${NC}"
    BASE_URL="${BASE_URL:-http://localhost:8080}"
    PRODUCT_CATALOG_URL="${PRODUCT_CATALOG_URL:-http://localhost:8081}"
    TEST_DURATION="${TEST_DURATION:-5m}"
fi

# Create results directory
mkdir -p "$RESULTS_DIR"

# Check if k6 is installed
if ! command -v k6 &> /dev/null; then
    echo -e "${RED}✗ k6 is not installed${NC}"
    echo "Please install k6 from: https://k6.io/docs/getting-started/installation/"
    echo ""
    echo "macOS:   brew install k6"
    echo "Linux:   sudo gpg -k && sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69 && echo \"deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main\" | sudo tee /etc/apt/sources.list.d/k6.list && sudo apt-get update && sudo apt-get install k6"
    echo "Windows: choco install k6"
    exit 1
fi

echo -e "${GREEN}✓ k6 is installed${NC}"

# Step 1: Fetch products from catalog
echo ""
echo "========================================="
echo "Step 1: Fetching Product Catalog"
echo "========================================="
bash "${SCRIPT_DIR}/fetch-products.sh"

# Verify products file exists
PRODUCTS_FILE="${SCRIPT_DIR}/../data/products.json"
if [ ! -f "$PRODUCTS_FILE" ]; then
    echo -e "${RED}✗ Products file not found at $PRODUCTS_FILE${NC}"
    exit 1
fi

# Step 2: Check Order Management API health
echo ""
echo "========================================="
echo "Step 2: Checking Order Management API"
echo "========================================="
if curl -f -s -o /dev/null "$BASE_URL/actuator/health" 2>/dev/null; then
    echo -e "${GREEN}✓ Order Management API is healthy at $BASE_URL${NC}"
else
    echo -e "${YELLOW}⚠ Warning: Order Management API may not be running at $BASE_URL${NC}"
    echo "  The load test will likely fail if the API is not available"
    read -p "  Do you want to continue anyway? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Step 3: Run k6 load test
echo ""
echo "========================================="
echo "Step 3: Running K6 Load Test"
echo "========================================="
echo "Configuration:"
echo "  Base URL: $BASE_URL"
echo "  Duration: $TEST_DURATION"
echo "  Rate: 2 orders/second (1 every 0.5s)"
echo ""

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
SUMMARY_FILE="${RESULTS_DIR}/summary_${TIMESTAMP}.json"
HTML_REPORT="${RESULTS_DIR}/report_${TIMESTAMP}.html"

# Run k6 test
k6 run \
  --out json="${RESULTS_DIR}/raw_${TIMESTAMP}.json" \
  -e BASE_URL="$BASE_URL" \
  -e PRODUCT_CATALOG_URL="$PRODUCT_CATALOG_URL" \
  "${SCRIPT_DIR}/order-load-test.js" \
  | tee "${RESULTS_DIR}/output_${TIMESTAMP}.log"

echo ""
echo "========================================="
echo "Load Test Complete!"
echo "========================================="
echo -e "${GREEN}✓ Results saved to: $RESULTS_DIR${NC}"
echo "  - Raw data: raw_${TIMESTAMP}.json"
echo "  - Output log: output_${TIMESTAMP}.log"
echo ""
echo "To view detailed metrics, you can:"
echo "  1. Check the output log above"
echo "  2. Analyze raw_${TIMESTAMP}.json with k6 cloud or other tools"
echo "  3. Use k6 web dashboard: k6 run --out web-dashboard ${SCRIPT_DIR}/order-load-test.js"
echo ""
