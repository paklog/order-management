package com.paklog.ordermanagement.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;

public interface FulfillmentOrderRepository {
    FulfillmentOrder saveOrder(FulfillmentOrder order);
    Optional<FulfillmentOrder> findById(UUID orderId);
    Optional<FulfillmentOrder> findBySellerFulfillmentOrderId(String sellerFulfillmentOrderId);
    Optional<FulfillmentOrder> findByIdempotencyKey(String idempotencyKey);
    void deleteById(UUID orderId);

    /**
     * Finds orders by displayable order ID within a time range.
     * Used for fuzzy duplicate detection.
     *
     * @param displayableOrderId the displayable order ID
     * @param startTime start of time range
     * @param endTime end of time range
     * @return list of matching orders
     */
    List<FulfillmentOrder> findByDisplayableOrderIdAndReceivedDateBetween(
        String displayableOrderId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );
}
