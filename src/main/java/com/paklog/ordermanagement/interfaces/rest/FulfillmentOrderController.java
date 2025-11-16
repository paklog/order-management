package com.paklog.ordermanagement.interfaces.rest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paklog.ordermanagement.application.service.EventPublisherService;
import com.paklog.ordermanagement.application.service.FulfillmentOrderService;
import com.paklog.ordermanagement.domain.event.FulfillmentOrderPartiallyAcceptedEvent;
import com.paklog.ordermanagement.domain.event.FulfillmentOrderStockUnavailableEvent;
import com.paklog.ordermanagement.domain.event.FulfillmentOrderValidatedEvent;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.FulfillmentPolicy;
import com.paklog.ordermanagement.domain.model.UnfulfillableItem;
import com.paklog.ordermanagement.domain.service.OrderValidationService;
import com.paklog.ordermanagement.interfaces.dto.CancelFulfillmentOrderRequest;
import com.paklog.ordermanagement.interfaces.dto.CreateFulfillmentOrderRequest;
import com.paklog.ordermanagement.interfaces.dto.FulfillmentOrderDto;

@RestController
@RequestMapping("/fulfillment_orders")
@Validated
public class FulfillmentOrderController {

    private static final Logger logger = LoggerFactory.getLogger(FulfillmentOrderController.class);

    private final FulfillmentOrderService fulfillmentOrderService;
    private final OrderValidationService orderValidationService;
    private final EventPublisherService eventPublisherService;

    public FulfillmentOrderController(FulfillmentOrderService fulfillmentOrderService,
                                     OrderValidationService orderValidationService,
                                     EventPublisherService eventPublisherService) {
        this.fulfillmentOrderService = fulfillmentOrderService;
        this.orderValidationService = orderValidationService;
        this.eventPublisherService = eventPublisherService;
    }

    @PostMapping
    public ResponseEntity<FulfillmentOrderDto> createFulfillmentOrder(
            @RequestHeader(value = "Idempotency-Key") @NotBlank(message = "Idempotency-Key header is required") String idempotencyKey,
            @Valid @RequestBody CreateFulfillmentOrderRequest request) {
        Instant startTime = Instant.now();
        logger.info("Creating new fulfillment order - SellerOrderId: {}, ItemCount: {}, Policy: {}",
                request.getSellerFulfillmentOrderId(),
                request.getItems() != null ? request.getItems().size() : 0,
                request.getFulfillmentPolicy());

        try {
            // Convert request to domain model
            FulfillmentOrder order = convertToDomain(request, idempotencyKey);
            logger.debug("Converted request to domain model - OrderId: {}", order.getOrderId());

            // Validate basic business rules (not inventory)
            if (!validateOrder(order)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Check inventory availability
            OrderValidationService.InventoryAvailabilityResult inventoryResult =
                orderValidationService.checkInventoryAvailability(order);

            // Apply fulfillment policy
            if (!applyFulfillmentPolicy(order, inventoryResult)) {
                // FILL_OR_KILL policy and items unavailable
                logger.warn("Order rejected due to FILL_OR_KILL policy - OrderId: {}, UnavailableItems: {}",
                    order.getOrderId(), inventoryResult.getUnfulfillableItems().size());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Publish validation success event
            publishValidationEvent(order);

            // Publish inventory-related events based on order state
            publishInventoryEvents(order);

            // Create and persist the order
            FulfillmentOrder createdOrder = fulfillmentOrderService.createOrder(order);
            logger.info("Successfully created fulfillment order - OrderId: {}, Status: {}, FulfillmentAction: {}",
                    createdOrder.getOrderId(), createdOrder.getStatus(), createdOrder.getFulfillmentAction());

            // Convert to DTO and return
            FulfillmentOrderDto dto = convertToDto(createdOrder);

            logCompletionMetrics(startTime, createdOrder.getOrderId());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);

        } catch (IllegalStateException e) {
            return handleConflictError(request, startTime, e);

        } catch (Exception e) {
            return handleGeneralError(request, startTime, e);
        }
    }

    /**
     * Validates the order against business rules.
     *
     * @param order the order to validate
     * @return true if validation passes, false otherwise
     */
    private boolean validateOrder(FulfillmentOrder order) {
        OrderValidationService.ValidationResult validationResult = orderValidationService.validate(order);
        if (!validationResult.isValid()) {
            logger.warn("Order validation failed - OrderId: {}, Errors: {}",
                order.getOrderId(), validationResult.getErrors());
            return false;
        }
        return true;
    }

    /**
     * Applies the fulfillment policy based on inventory availability.
     *
     * @param order the order to process
     * @param inventoryResult the inventory availability result
     * @return true if order should be accepted, false if rejected
     */
    private boolean applyFulfillmentPolicy(FulfillmentOrder order,
                                          OrderValidationService.InventoryAvailabilityResult inventoryResult) {
        FulfillmentPolicy policy = order.getFulfillmentPolicy();

        if (inventoryResult.isAllAvailable()) {
            // All items available, no need to apply policy restrictions
            logger.debug("All items available - OrderId: {}, Policy: {}", order.getOrderId(), policy);
            return true;
        }

        // Items are unavailable, apply policy
        for (UnfulfillableItem item : inventoryResult.getUnfulfillableItems()) {
            order.addUnfulfillableItem(item);
        }

        switch (policy) {
            case FILL_OR_KILL:
                // Reject the entire order
                logger.info("FILL_OR_KILL policy: Rejecting order due to unavailable items - OrderId: {}",
                    order.getOrderId());
                return false;

            case FILL_ALL:
                // Accept the order, will publish stock unavailable event
                logger.info("FILL_ALL policy: Accepting order despite unavailable items - OrderId: {}, UnavailableCount: {}",
                    order.getOrderId(), inventoryResult.getUnfulfillableItems().size());
                return true;

            case FILL_ALL_AVAILABLE:
                // Accept the order with partial fulfillment
                logger.info("FILL_ALL_AVAILABLE policy: Accepting order for partial fulfillment - OrderId: {}, UnavailableCount: {}",
                    order.getOrderId(), inventoryResult.getUnfulfillableItems().size());
                return true;

            default:
                logger.error("Unknown fulfillment policy - OrderId: {}, Policy: {}", order.getOrderId(), policy);
                return false;
        }
    }

    /**
     * Publishes inventory-related events based on order state.
     *
     * @param order the order with inventory state
     */
    private void publishInventoryEvents(FulfillmentOrder order) {
        if (!order.hasUnfulfillableItems()) {
            return;
        }

        FulfillmentPolicy policy = order.getFulfillmentPolicy();

        if (policy == FulfillmentPolicy.FILL_ALL_AVAILABLE && order.isPartiallyFulfillable()) {
            // Publish partial fulfillment event
            logger.debug("Publishing FulfillmentOrderPartiallyAcceptedEvent - OrderId: {}", order.getOrderId());
            FulfillmentOrderPartiallyAcceptedEvent partialEvent =
                new FulfillmentOrderPartiallyAcceptedEvent(order);
            eventPublisherService.publishEvent(partialEvent);
            logger.info("Partial fulfillment event published - OrderId: {}, UnfulfillableItems: {}",
                order.getOrderId(), order.getUnfulfillableItems().size());
        }

        if (policy == FulfillmentPolicy.FILL_ALL || order.hasUnfulfillableItems()) {
            // Publish stock unavailable event
            logger.debug("Publishing FulfillmentOrderStockUnavailableEvent - OrderId: {}", order.getOrderId());
            FulfillmentOrderStockUnavailableEvent stockEvent =
                new FulfillmentOrderStockUnavailableEvent(order);
            eventPublisherService.publishEvent(stockEvent);
            logger.info("Stock unavailable event published - OrderId: {}, UnavailableItems: {}",
                order.getOrderId(), order.getUnfulfillableItems().size());
        }
    }

    /**
     * Publishes validation success event.
     *
     * @param order the validated order
     */
    private void publishValidationEvent(FulfillmentOrder order) {
        logger.debug("Publishing FulfillmentOrderValidatedEvent - OrderId: {}", order.getOrderId());
        FulfillmentOrderValidatedEvent validatedEvent = new FulfillmentOrderValidatedEvent(order);
        eventPublisherService.publishEvent(validatedEvent);
        logger.info("Order validation successful - OrderId: {}", order.getOrderId());
    }

    /**
     * Logs completion metrics.
     *
     * @param startTime when the operation started
     * @param orderId the order ID
     */
    private void logCompletionMetrics(Instant startTime, UUID orderId) {
        Duration duration = Duration.between(startTime, Instant.now());
        logger.info("Create fulfillment order completed - OrderId: {}, Duration: {}ms",
                orderId, duration.toMillis());
    }

    /**
     * Handles conflict errors (duplicate orders).
     *
     * @param request the original request
     * @param startTime when the operation started
     * @param e the exception
     * @return HTTP 409 Conflict response
     */
    private ResponseEntity<FulfillmentOrderDto> handleConflictError(
            CreateFulfillmentOrderRequest request, Instant startTime, IllegalStateException e) {
        Duration duration = Duration.between(startTime, Instant.now());
        logger.warn("Conflict creating fulfillment order - SellerOrderId: {}, Error: {}, Duration: {}ms",
                request.getSellerFulfillmentOrderId(), e.getMessage(), duration.toMillis());
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    /**
     * Handles general errors.
     *
     * @param request the original request
     * @param startTime when the operation started
     * @param e the exception
     * @return HTTP 400 Bad Request response
     */
    private ResponseEntity<FulfillmentOrderDto> handleGeneralError(
            CreateFulfillmentOrderRequest request, Instant startTime, Exception e) {
        Duration duration = Duration.between(startTime, Instant.now());
        logger.error("Failed to create fulfillment order - SellerOrderId: {}, Error: {}, Duration: {}ms",
                request.getSellerFulfillmentOrderId(), e.getMessage(), duration.toMillis(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
            idempotencyKey,
            request.getFulfillmentPolicy()
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
        dto.setFulfillmentPolicy(order.getFulfillmentPolicy());
        dto.setFulfillmentAction(order.getFulfillmentAction());
        // Note: unfulfillableItems are communicated asynchronously via domain events
        return dto;
    }
}
