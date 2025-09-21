package com.paklog.ordermanagement.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;

public interface FulfillmentOrderRepository {
    FulfillmentOrder saveOrder(FulfillmentOrder order);
    Optional<FulfillmentOrder> findById(UUID orderId);
    Optional<FulfillmentOrder> findBySellerFulfillmentOrderId(String sellerFulfillmentOrderId);
    Optional<FulfillmentOrder> findByIdempotencyKey(String idempotencyKey);
    void deleteById(UUID orderId);
}
