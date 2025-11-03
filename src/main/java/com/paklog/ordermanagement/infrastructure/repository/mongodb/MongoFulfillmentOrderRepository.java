package com.paklog.ordermanagement.infrastructure.repository.mongodb;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.repository.FulfillmentOrderRepository;

@Repository
public interface MongoFulfillmentOrderRepository extends MongoRepository<FulfillmentOrder, UUID>, FulfillmentOrderRepository {
    Optional<FulfillmentOrder> findBySellerFulfillmentOrderId(String sellerFulfillmentOrderId);
    Optional<FulfillmentOrder> findByIdempotencyKey(String idempotencyKey);

    /**
     * Finds orders by displayable order ID within a time range.
     * Spring Data MongoDB will automatically implement this based on method naming convention.
     */
    List<FulfillmentOrder> findByDisplayableOrderIdAndReceivedDateBetween(
        String displayableOrderId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );

    default FulfillmentOrder saveOrder(FulfillmentOrder order) {
        return save(order);
    }
}
