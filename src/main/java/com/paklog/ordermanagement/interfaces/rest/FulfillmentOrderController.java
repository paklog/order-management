package com.paklog.ordermanagement.interfaces.rest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paklog.ordermanagement.application.service.FulfillmentOrderService;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.interfaces.dto.CancelFulfillmentOrderRequest;
import com.paklog.ordermanagement.interfaces.dto.CreateFulfillmentOrderRequest;
import com.paklog.ordermanagement.interfaces.dto.FulfillmentOrderDto;

@RestController
@RequestMapping("/fulfillment_orders")
public class FulfillmentOrderController {

    private static final Logger logger = LoggerFactory.getLogger(FulfillmentOrderController.class);

    private final FulfillmentOrderService fulfillmentOrderService;

    public FulfillmentOrderController(FulfillmentOrderService fulfillmentOrderService) {
        this.fulfillmentOrderService = fulfillmentOrderService;
    }

    @PostMapping
    public ResponseEntity<FulfillmentOrderDto> createFulfillmentOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateFulfillmentOrderRequest request) {
        Instant startTime = Instant.now();
        logger.info("Creating new fulfillment order - SellerOrderId: {}, ItemCount: {}",
                request.getSellerFulfillmentOrderId(),
                request.getItems() != null ? request.getItems().size() : 0);

        try {
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            // Convert request to domain model
            FulfillmentOrder order = convertToDomain(request, idempotencyKey);
            logger.debug("Converted request to domain model - OrderId: {}", order.getOrderId());

            // Create the order
            FulfillmentOrder createdOrder = fulfillmentOrderService.createOrder(order);
            logger.info("Successfully created fulfillment order - OrderId: {}, Status: {}",
                    createdOrder.getOrderId(), createdOrder.getStatus());

            // Convert to DTO and return
            FulfillmentOrderDto dto = convertToDto(createdOrder);

            Duration duration = Duration.between(startTime, Instant.now());
            logger.info("Create fulfillment order completed - OrderId: {}, Duration: {}ms",
                    createdOrder.getOrderId(), duration.toMillis());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);

        } catch (IllegalStateException e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.warn("Conflict creating fulfillment order - SellerOrderId: {}, Error: {}, Duration: {}ms",
                    request.getSellerFulfillmentOrderId(), e.getMessage(), duration.toMillis());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Failed to create fulfillment order - SellerOrderId: {}, Error: {}, Duration: {}ms",
                    request.getSellerFulfillmentOrderId(), e.getMessage(), duration.toMillis(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{order_id}")
    public ResponseEntity<FulfillmentOrderDto> getFulfillmentOrderById(@PathVariable("order_id") UUID orderId) {
        Instant startTime = Instant.now();
        logger.info("Retrieving fulfillment order - OrderId: {}", orderId);

        try {
            return fulfillmentOrderService.getOrderById(orderId)
                .map(order -> {
                    Duration duration = Duration.between(startTime, Instant.now());
                    logger.info("Successfully retrieved fulfillment order - OrderId: {}, Status: {}, Duration: {}ms",
                            orderId, order.getStatus(), duration.toMillis());
                    return ResponseEntity.ok(convertToDto(order));
                })
                .orElseGet(() -> {
                    Duration duration = Duration.between(startTime, Instant.now());
                    logger.warn("Fulfillment order not found - OrderId: {}, Duration: {}ms",
                            orderId, duration.toMillis());
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Failed to retrieve fulfillment order - OrderId: {}, Error: {}, Duration: {}ms",
                    orderId, e.getMessage(), duration.toMillis(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{order_id}/cancel")
    public ResponseEntity<Void> cancelFulfillmentOrder(@PathVariable("order_id") UUID orderId,
                                                      @RequestBody CancelFulfillmentOrderRequest request) {
        Instant startTime = Instant.now();
        logger.info("Cancelling fulfillment order - OrderId: {}, Reason: {}",
                orderId, request.getCancellationReason());

        try {
            fulfillmentOrderService.cancelOrder(orderId, request.getCancellationReason());

            Duration duration = Duration.between(startTime, Instant.now());
            logger.info("Successfully cancelled fulfillment order - OrderId: {}, Duration: {}ms",
                    orderId, duration.toMillis());

            return ResponseEntity.status(HttpStatus.ACCEPTED).build();

        } catch (IllegalArgumentException e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.warn("Fulfillment order not found for cancellation - OrderId: {}, Error: {}, Duration: {}ms",
                    orderId, e.getMessage(), duration.toMillis());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.warn("Cannot cancel fulfillment order - OrderId: {}, Error: {}, Duration: {}ms",
                    orderId, e.getMessage(), duration.toMillis());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Failed to cancel fulfillment order - OrderId: {}, Error: {}, Duration: {}ms",
                    orderId, e.getMessage(), duration.toMillis(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods for conversion
    private FulfillmentOrder convertToDomain(CreateFulfillmentOrderRequest request, String idempotencyKey) {
        return new FulfillmentOrder(
            UUID.randomUUID(),
            request.getSellerFulfillmentOrderId(),
            request.getDisplayableOrderId(),
            request.getDisplayableOrderDate(),
            request.getDisplayableOrderComment(),
            request.getShippingSpeedCategory(),
            request.getDestinationAddress(),
            request.getItems(),
            idempotencyKey
        );
    }

    private FulfillmentOrderDto convertToDto(FulfillmentOrder order) {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setSellerFulfillmentOrderId(order.getSellerFulfillmentOrderId());
        dto.setDisplayableOrderId(order.getDisplayableOrderId());
        dto.setDisplayableOrderDate(order.getDisplayableOrderDate());
        dto.setDisplayableOrderComment(order.getDisplayableOrderComment());
        dto.setShippingSpeedCategory(order.getShippingSpeedCategory());
        dto.setDestinationAddress(order.getDestinationAddress());
        dto.setStatus(order.getStatus());
        dto.setItems(order.getItems());
        dto.setReceivedDate(order.getReceivedDate());
        dto.setCancellationReason(order.getCancellationReason());
        return dto;
    }
}
