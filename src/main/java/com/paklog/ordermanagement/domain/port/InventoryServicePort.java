package com.paklog.ordermanagement.domain.port;

import java.util.List;
import java.util.Map;

/**
 * Port interface for Inventory Service integration.
 * Defines operations needed to check inventory availability for order validation.
 *
 * This is a hexagonal architecture port - implementations will be in infrastructure layer.
 */
public interface InventoryServicePort {

    /**
     * Checks if all items in the order have sufficient inventory.
     *
     * @param items map of SKU to required quantity
     * @return InventoryCheckResult containing availability details
     */
    InventoryCheckResult checkAvailability(Map<String, Integer> items);

    /**
     * Checks availability for a single SKU.
     *
     * @param sku the product SKU
     * @param quantity required quantity
     * @return true if sufficient inventory available
     */
    boolean isAvailable(String sku, int quantity);

    /**
     * Result of inventory availability check.
     */
    class InventoryCheckResult {
        private final boolean allAvailable;
        private final List<UnavailableItem> unavailableItems;
        private final String message;

        public InventoryCheckResult(boolean allAvailable, List<UnavailableItem> unavailableItems, String message) {
            this.allAvailable = allAvailable;
            this.unavailableItems = unavailableItems;
            this.message = message;
        }

        public static InventoryCheckResult available() {
            return new InventoryCheckResult(true, List.of(), "All items available");
        }

        public static InventoryCheckResult unavailable(List<UnavailableItem> items, String message) {
            return new InventoryCheckResult(false, items, message);
        }

        public boolean isAllAvailable() {
            return allAvailable;
        }

        public List<UnavailableItem> getUnavailableItems() {
            return unavailableItems;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Details of an unavailable item.
     */
    class UnavailableItem {
        private final String sku;
        private final int requested;
        private final int available;

        public UnavailableItem(String sku, int requested, int available) {
            this.sku = sku;
            this.requested = requested;
            this.available = available;
        }

        public String getSku() {
            return sku;
        }

        public int getRequested() {
            return requested;
        }

        public int getAvailable() {
            return available;
        }

        public int getShortfall() {
            return requested - available;
        }
    }
}
