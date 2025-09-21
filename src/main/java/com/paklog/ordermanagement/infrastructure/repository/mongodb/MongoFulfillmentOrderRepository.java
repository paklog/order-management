package com.paklog.ordermanagement.infrastructure.repository.mongodb;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.repository.FulfillmentOrderRepository;

@Repository
public interface MongoFulfillmentOrderRepository extends MongoRepository<FulfillmentOrder, UUID>, FulfillmentOrderRepository {
    Optional<FulfillmentOrder> findBySellerFulfillmentOrderId(String sellerFulfillmentOrderId);

    default FulfillmentOrder saveOrder(FulfillmentOrder order) {
        return save(order);
    }
}