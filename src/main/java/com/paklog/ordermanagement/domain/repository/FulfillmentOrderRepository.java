package com.paklog.ordermanagement.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;

public interface FulfillmentOrderRepository {
    FulfillmentOrder save(FulfillmentOrder order);
    Optional<FulfillmentOrder> findById(UUID orderId);
    Optional<FulfillmentOrder> findBySellerFulfillmentOrderId(String sellerFulfillmentOrderId);
    void deleteById(UUID orderId);
}