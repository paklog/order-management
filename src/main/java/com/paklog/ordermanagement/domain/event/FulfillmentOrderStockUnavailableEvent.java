package com.paklog.ordermanagement.domain.event;

import java.util.List;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.UnfulfillableItem;

/**
 * Event published when a fulfillment order has stock unavailable for some or all items.
 *
 * This event is published when the order is accepted (based on FILL_ALL policy)
 * but items are unavailable. It notifies downstream systems about the stock
 * shortage so they can take appropriate action (e.g., trigger reorder, notify customer).
 */
public class FulfillmentOrderStockUnavailableEvent extends FulfillmentOrderEvent {

    public static final String EVENT_TYPE = "com.paklog.fulfillment.order.stock_unavailable";

    public FulfillmentOrderStockUnavailableEvent() {
        super();
        this.type = EVENT_TYPE;
    }

    public FulfillmentOrderStockUnavailableEvent(FulfillmentOrder order) {
        super(order);
        this.type = EVENT_TYPE;
        this.data = new StockUnavailableData(order);
    }

    public static class StockUnavailableData {
        private FulfillmentOrder order;
        private List<UnfulfillableItem> unavailableItems;
        private int totalItemsRequested;
        private int itemsUnavailable;
        private int totalQuantityShortfall;
        private String summary;

        public StockUnavailableData() {
        }

        public StockUnavailableData(FulfillmentOrder order) {
            this.order = order;
            this.unavailableItems = order.getUnfulfillableItems();
            this.totalItemsRequested = order.getItems().size();
            this.itemsUnavailable = unavailableItems != null ? unavailableItems.size() : 0;
            this.totalQuantityShortfall = calculateTotalShortfall();
            this.summary = String.format(
                "Order %s accepted with stock shortage: %d of %d items unavailable, total shortfall: %d units",
                order.getOrderId(),
                itemsUnavailable,
                totalItemsRequested,
                totalQuantityShortfall
            );
        }

        private int calculateTotalShortfall() {
            if (unavailableItems == null || unavailableItems.isEmpty()) {
                return 0;
            }
            return unavailableItems.stream()
                .mapToInt(UnfulfillableItem::getUnfulfillableQuantity)
                .sum();
        }

        public FulfillmentOrder getOrder() {
            return order;
        }

        public void setOrder(FulfillmentOrder order) {
            this.order = order;
        }

        public List<UnfulfillableItem> getUnavailableItems() {
            return unavailableItems;
        }

        public void setUnavailableItems(List<UnfulfillableItem> unavailableItems) {
            this.unavailableItems = unavailableItems;
        }

        public int getTotalItemsRequested() {
            return totalItemsRequested;
        }

        public void setTotalItemsRequested(int totalItemsRequested) {
            this.totalItemsRequested = totalItemsRequested;
        }

        public int getItemsUnavailable() {
            return itemsUnavailable;
        }

        public void setItemsUnavailable(int itemsUnavailable) {
            this.itemsUnavailable = itemsUnavailable;
        }

        public int getTotalQuantityShortfall() {
            return totalQuantityShortfall;
        }

        public void setTotalQuantityShortfall(int totalQuantityShortfall) {
            this.totalQuantityShortfall = totalQuantityShortfall;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }
}
