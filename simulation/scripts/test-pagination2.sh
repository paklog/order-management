#!/bin/bash
ALL_PRODUCTS="[]"
PAGE=0
HAS_MORE=true

while [ "$HAS_MORE" = true ]; do
    echo "Fetching page $PAGE (default size)..."
    RESPONSE=$(curl -s "http://localhost:8082/products?page=$PAGE")
    
    if [ -n "$RESPONSE" ]; then
        PAGE_PRODUCTS=$(echo "$RESPONSE" | jq '.content // []')
        PRODUCT_COUNT=$(echo "$PAGE_PRODUCTS" | jq 'length')
        echo "  Got $PRODUCT_COUNT products from page $PAGE"
        
        ALL_PRODUCTS=$(echo "$ALL_PRODUCTS" "$PAGE_PRODUCTS" | jq -s 'add')
        
        IS_LAST=$(echo "$RESPONSE" | jq -r '.is_last // true')
        TOTAL_ELEMENTS=$(echo "$RESPONSE" | jq -r '.total_elements')
        echo "  is_last: $IS_LAST, total_elements: $TOTAL_ELEMENTS"
        
        if [ "$IS_LAST" = "true" ]; then
            HAS_MORE=false
        else
            PAGE=$((PAGE + 1))
        fi
    else
        HAS_MORE=false
    fi
done

TOTAL_PRODUCTS=$(echo "$ALL_PRODUCTS" | jq 'length')
echo "===== Total products fetched: $TOTAL_PRODUCTS ====="
