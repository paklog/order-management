package com.paklog.ordermanagement.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paklog.ordermanagement.domain.event.FulfillmentOrderCancelledEvent;
import com.paklog.ordermanagement.domain.event.FulfillmentOrderReceivedEvent;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.repository.FulfillmentOrderRepository;

@Service
public class FulfillmentOrderService {

    private static final Logger logger = LoggerFactory.getLogger(FulfillmentOrderService.class);

    private final FulfillmentOrderRepository fulfillmentOrderRepository;
    private final EventPublisherService eventPublisherService;

    public FulfillmentOrderService(FulfillmentOrderRepository fulfillmentOrderRepository,
                                  EventPublisherService eventPublisherService) {
        this.fulfillmentOrderRepository = fulfillmentOrderRepository;
        this.eventPublisherService = eventPublisherService;
        logger.info("FulfillmentOrderService initialized");
    }

    @Transactional
    public FulfillmentOrder createOrder(FulfillmentOrder order) {
        Instant startTime = Instant.now();
        logger.info("Creating fulfillment order - OrderId: {}, SellerOrderId: {}, ItemCount: {}",
                order.getOrderId(),
                order.getSellerFulfillmentOrderId(),
                order.getItems() != null ? order.getItems().size() : 0);

        try {
            // Check for idempotent replay
        Optional<FulfillmentOrder> existingByKey =
            fulfillmentOrderRepository.findByIdempotencyKey(order.getIdempotencyKey());

        if (existingByKey.isPresent()) {
            return existingByKey.get();
        }

        // Check if order with same sellerFulfillmentOrderId already exists
            logger.debug("Checking for existing order with SellerOrderId: {}", order.getSellerFulfillmentOrderId());
            Optional<FulfillmentOrder> existingOrder =
                fulfillmentOrderRepository.findBySellerFulfillmentOrderId(order.getSellerFulfillmentOrderId());

            if (existingOrder.isPresent()) {
                Duration duration = Duration.between(startTime, Instant.now());
                logger.warn("Duplicate order creation attempt - SellerOrderId: {}, ExistingOrderId: {}, Duration: {}ms",
                        order.getSellerFulfillmentOrderId(),
                        existingOrder.get().getOrderId(),
                        duration.toMillis());
                throw new IllegalStateException("Order with sellerFulfillmentOrderId already exists");
            }

            // Receive the order
            logger.debug("Processing order transition to RECEIVED - OrderId: {}", order.getOrderId());
            order.receive();

            // Save the order
            logger.debug("Persisting fulfillment order to database - OrderId: {}", order.getOrderId());
            FulfillmentOrder savedOrder = fulfillmentOrderRepository.saveOrder(order);

            // Publish event
            logger.debug("Publishing FulfillmentOrderReceivedEvent - OrderId: {}", savedOrder.getOrderId());
            FulfillmentOrderReceivedEvent event = new FulfillmentOrderReceivedEvent(savedOrder);
            eventPublisherService.publishEvent(event);

            Duration duration = Duration.between(startTime, Instant.now());
            logger.info("Successfully created fulfillment order - OrderId: {}, Status: {}, Duration: {}ms",
                    savedOrder.getOrderId(), savedOrder.getStatus(), duration.toMillis());

            return savedOrder;

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Failed to create fulfillment order - OrderId: {}, SellerOrderId: {}, Error: {}, Duration: {}ms",
                    order.getOrderId(), order.getSellerFulfillmentOrderId(), e.getMessage(), duration.toMillis(), e);
            throw e;
        }
    }

    public Optional<FulfillmentOrder> getOrderById(UUID orderId) {
        Instant startTime = Instant.now();
        logger.debug("Retrieving fulfillment order from database - OrderId: {}", orderId);

        try {
            Optional<FulfillmentOrder> order = fulfillmentOrderRepository.findById(orderId);
            Duration duration = Duration.between(startTime, Instant.now());

            if (order.isPresent()) {
                logger.debug("Successfully retrieved fulfillment order - OrderId: {}, Status: {}, Duration: {}ms",
                        orderId, order.get().getStatus(), duration.toMillis());
            } else {
                logger.debug("Fulfillment order not found in database - OrderId: {}, Duration: {}ms",
                        orderId, duration.toMillis());
            }

            return order;

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Failed to retrieve fulfillment order - OrderId: {}, Error: {}, Duration: {}ms",
                    orderId, e.getMessage(), duration.toMillis(), e);
            throw e;
        }
    }

    @Transactional
    public FulfillmentOrder cancelOrder(UUID orderId, String cancellationReason) {
        Instant startTime = Instant.now();
        logger.info("Cancelling fulfillment order - OrderId: {}, Reason: {}", orderId, cancellationReason);

        try {
            logger.debug("Retrieving order for cancellation - OrderId: {}", orderId);
            FulfillmentOrder order = fulfillmentOrderRepository.findById(orderId)
                .orElseThrow(() -> {
                    Duration duration = Duration.between(startTime, Instant.now());
                    logger.warn("Order not found for cancellation - OrderId: {}, Duration: {}ms",
                            orderId, duration.toMillis());
                    return new IllegalArgumentException("Order not found");
                });

            logger.debug("Processing order cancellation - OrderId: {}, CurrentStatus: {}",
                    orderId, order.getStatus());
            order.cancel(cancellationReason);

            logger.debug("Persisting cancelled order - OrderId: {}", orderId);
            FulfillmentOrder savedOrder = fulfillmentOrderRepository.saveOrder(order);

            // Publish cancellation event
            logger.debug("Publishing FulfillmentOrderCancelledEvent - OrderId: {}", orderId);
            FulfillmentOrderCancelledEvent event = new FulfillmentOrderCancelledEvent(
                savedOrder.getOrderId().toString(),
                savedOrder.getSellerFulfillmentOrderId(),
                savedOrder.getCancellationReason()
            );
            eventPublisherService.publishEvent(event);

            Duration duration = Duration.between(startTime, Instant.now());
            logger.info("Successfully cancelled fulfillment order - OrderId: {}, Status: {}, Duration: {}ms",
                    savedOrder.getOrderId(), savedOrder.getStatus(), duration.toMillis());

            return savedOrder;

        } catch (IllegalStateException e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.warn("Cannot cancel order due to invalid state - OrderId: {}, Error: {}, Duration: {}ms",
                    orderId, e.getMessage(), duration.toMillis());
            throw e;
        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Failed to cancel fulfillment order - OrderId: {}, Error: {}, Duration: {}ms",
                    orderId, e.getMessage(), duration.toMillis(), e);
            throw e;
        }
    }
}
