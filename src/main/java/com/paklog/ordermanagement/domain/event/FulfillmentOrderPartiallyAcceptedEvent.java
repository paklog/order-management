package com.paklog.ordermanagement.domain.event;

import java.util.List;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.UnfulfillableItem;

/**
 * Event published when a fulfillment order is accepted with partial fulfillment.
 *
 * This event indicates that the order has been accepted but not all items
 * can be fulfilled due to inventory constraints. Only available items will
 * be processed.
 */
public class FulfillmentOrderPartiallyAcceptedEvent extends FulfillmentOrderEvent {

    public static final String EVENT_TYPE = "com.paklog.fulfillment.order.partially_accepted";

    public FulfillmentOrderPartiallyAcceptedEvent() {
        super();
        this.type = EVENT_TYPE;
    }

    public FulfillmentOrderPartiallyAcceptedEvent(FulfillmentOrder order) {
        super(order);
        this.type = EVENT_TYPE;
        this.data = new PartialFulfillmentData(order);
    }

    public static class PartialFulfillmentData {
        private FulfillmentOrder order;
        private List<UnfulfillableItem> unfulfillableItems;
        private int totalItemsRequested;
        private int itemsFulfillable;
        private int itemsUnfulfillable;
        private String summary;

        public PartialFulfillmentData() {
        }

        public PartialFulfillmentData(FulfillmentOrder order) {
            this.order = order;
            this.unfulfillableItems = order.getUnfulfillableItems();
            this.totalItemsRequested = order.getItems().size();
            this.itemsUnfulfillable = unfulfillableItems != null ? unfulfillableItems.size() : 0;
            this.itemsFulfillable = totalItemsRequested - itemsUnfulfillable;
            this.summary = String.format(
                "Order %s partially accepted: %d of %d items can be fulfilled",
                order.getOrderId(),
                itemsFulfillable,
                totalItemsRequested
            );
        }

        public FulfillmentOrder getOrder() {
            return order;
        }

        public void setOrder(FulfillmentOrder order) {
            this.order = order;
        }

        public List<UnfulfillableItem> getUnfulfillableItems() {
            return unfulfillableItems;
        }

        public void setUnfulfillableItems(List<UnfulfillableItem> unfulfillableItems) {
            this.unfulfillableItems = unfulfillableItems;
        }

        public int getTotalItemsRequested() {
            return totalItemsRequested;
        }

        public void setTotalItemsRequested(int totalItemsRequested) {
            this.totalItemsRequested = totalItemsRequested;
        }

        public int getItemsFulfillable() {
            return itemsFulfillable;
        }

        public void setItemsFulfillable(int itemsFulfillable) {
            this.itemsFulfillable = itemsFulfillable;
        }

        public int getItemsUnfulfillable() {
            return itemsUnfulfillable;
        }

        public void setItemsUnfulfillable(int itemsUnfulfillable) {
            this.itemsUnfulfillable = itemsUnfulfillable;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }
}
