package com.paklog.ordermanagement.domain.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paklog.ordermanagement.domain.config.OrderValidationConfig;
import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.repository.FulfillmentOrderRepository;

/**
 * Domain service for detecting duplicate orders using multiple criteria.
 * Implements fuzzy matching to prevent duplicate order submissions.
 */
@Service
public class DuplicateOrderDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(DuplicateOrderDetectionService.class);

    private final FulfillmentOrderRepository orderRepository;
    private final OrderValidationConfig config;
    private final Duration duplicateWindow;

    public DuplicateOrderDetectionService(FulfillmentOrderRepository orderRepository,
                                          OrderValidationConfig config) {
        this.orderRepository = orderRepository;
        this.config = config;
        this.duplicateWindow = Duration.ofHours(config.getDuplicateDetectionWindowHours());
        logger.info("DuplicateOrderDetectionService initialized - Detection window: {} hours",
            config.getDuplicateDetectionWindowHours());
    }

    /**
     * Checks if an order is a potential duplicate based on multiple criteria.
     *
     * @param order the order to check
     * @return DuplicateCheckResult containing duplicate status and details
     */
    public DuplicateCheckResult checkForDuplicate(FulfillmentOrder order) {
        logger.debug("Performing duplicate order check - OrderId: {}", order.getOrderId());

        // 1. Check by idempotency key (exact match)
        if (order.getIdempotencyKey() != null) {
            Optional<FulfillmentOrder> existingByKey =
                orderRepository.findByIdempotencyKey(order.getIdempotencyKey());

            if (existingByKey.isPresent()) {
                logger.info("Duplicate detected by idempotency key - Key: {}, ExistingOrderId: {}",
                    order.getIdempotencyKey(), existingByKey.get().getOrderId());
                return DuplicateCheckResult.duplicate(
                    DuplicateReason.IDEMPOTENCY_KEY,
                    existingByKey.get(),
                    "Idempotent replay - order already processed with this key"
                );
            }
        }

        // 2. Check by seller order ID (exact match)
        if (order.getSellerFulfillmentOrderId() != null) {
            Optional<FulfillmentOrder> existingBySellerId =
                orderRepository.findBySellerFulfillmentOrderId(order.getSellerFulfillmentOrderId());

            if (existingBySellerId.isPresent()) {
                logger.warn("Duplicate detected by seller order ID - SellerOrderId: {}, ExistingOrderId: {}",
                    order.getSellerFulfillmentOrderId(), existingBySellerId.get().getOrderId());
                return DuplicateCheckResult.duplicate(
                    DuplicateReason.SELLER_ORDER_ID,
                    existingBySellerId.get(),
                    "Order with same seller order ID already exists"
                );
            }
        }

        // 3. Check by fuzzy match (displayable order ID + address + similar timestamp)
        // This catches accidental resubmissions with different seller order IDs
        DuplicateCheckResult fuzzyResult = checkFuzzyDuplicate(order);
        if (fuzzyResult.isDuplicate()) {
            return fuzzyResult;
        }

        logger.debug("No duplicate detected - OrderId: {}", order.getOrderId());
        return DuplicateCheckResult.notDuplicate();
    }

    /**
     * Performs fuzzy matching to detect potential duplicate orders.
     * Considers orders with same displayable ID and address within configured time window as potential duplicates.
     */
    private DuplicateCheckResult checkFuzzyDuplicate(FulfillmentOrder order) {
        if (order.getDisplayableOrderId() == null || order.getDisplayableOrderId().isBlank()) {
            logger.debug("Skipping fuzzy duplicate check - No displayable order ID");
            return DuplicateCheckResult.notDuplicate();
        }

        try {
            // Calculate time window for fuzzy matching
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = now.minus(duplicateWindow);

            logger.debug("Fuzzy duplicate check - DisplayableOrderId: {}, Window: {} to {}",
                order.getDisplayableOrderId(), windowStart, now);

            // Query for orders with same displayable ID within time window
            List<FulfillmentOrder> potentialDuplicates = orderRepository
                .findByDisplayableOrderIdAndReceivedDateBetween(
                    order.getDisplayableOrderId(),
                    windowStart,
                    now
                );

            if (potentialDuplicates.isEmpty()) {
                logger.debug("No fuzzy duplicates found for displayable order ID: {}",
                    order.getDisplayableOrderId());
                return DuplicateCheckResult.notDuplicate();
            }

            // Check each potential duplicate for address similarity
            for (FulfillmentOrder existingOrder : potentialDuplicates) {
                // Skip if it's the same order (by ID)
                if (existingOrder.getOrderId() != null &&
                    existingOrder.getOrderId().equals(order.getOrderId())) {
                    continue;
                }

                // Check if addresses are similar
                if (isSimilarAddress(order.getDestinationAddress(),
                                    existingOrder.getDestinationAddress())) {

                    // Check if item counts match (additional fuzzy match criteria)
                    int orderItemCount = order.getItems() != null ? order.getItems().size() : 0;
                    int existingItemCount = existingOrder.getItems() != null ?
                        existingOrder.getItems().size() : 0;

                    if (orderItemCount == existingItemCount) {
                        logger.warn("Fuzzy duplicate detected - DisplayableOrderId: {}, " +
                            "ExistingOrderId: {}, MatchedOn: address+itemCount",
                            order.getDisplayableOrderId(), existingOrder.getOrderId());

                        return DuplicateCheckResult.duplicate(
                            DuplicateReason.FUZZY_MATCH,
                            existingOrder,
                            String.format("Similar order found with same displayable ID (%s), " +
                                "matching address, and same item count (%d) within %d hours",
                                order.getDisplayableOrderId(), orderItemCount,
                                config.getDuplicateDetectionWindowHours())
                        );
                    }
                }
            }

            logger.debug("No fuzzy duplicate match found despite {} candidates",
                potentialDuplicates.size());
            return DuplicateCheckResult.notDuplicate();

        } catch (Exception e) {
            logger.error("Error during fuzzy duplicate check - OrderId: {}, Error: {}",
                order.getOrderId(), e.getMessage(), e);
            // Fail open - don't block order processing due to fuzzy match errors
            return DuplicateCheckResult.notDuplicate();
        }
    }

    /**
     * Checks if two addresses are similar enough to be considered duplicates.
     */
    private boolean isSimilarAddress(Address addr1, Address addr2) {
        if (addr1 == null || addr2 == null) {
            return false;
        }

        // Normalize and compare key fields
        boolean sameRecipient = normalizeString(addr1.getName())
            .equals(normalizeString(addr2.getName()));

        boolean sameStreet = normalizeString(addr1.getAddressLine1())
            .equals(normalizeString(addr2.getAddressLine1()));

        boolean samePostalCode = normalizeString(addr1.getPostalCode())
            .equals(normalizeString(addr2.getPostalCode()));

        return sameRecipient && sameStreet && samePostalCode;
    }

    /**
     * Normalizes string for comparison (lowercase, trim, remove extra spaces).
     */
    private String normalizeString(String str) {
        if (str == null) {
            return "";
        }
        return str.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    /**
     * Result of duplicate order check.
     */
    public static class DuplicateCheckResult {
        private final boolean duplicate;
        private final DuplicateReason reason;
        private final FulfillmentOrder existingOrder;
        private final String message;

        private DuplicateCheckResult(boolean duplicate, DuplicateReason reason,
                                     FulfillmentOrder existingOrder, String message) {
            this.duplicate = duplicate;
            this.reason = reason;
            this.existingOrder = existingOrder;
            this.message = message;
        }

        public static DuplicateCheckResult duplicate(DuplicateReason reason,
                                                     FulfillmentOrder existingOrder,
                                                     String message) {
            return new DuplicateCheckResult(true, reason, existingOrder, message);
        }

        public static DuplicateCheckResult notDuplicate() {
            return new DuplicateCheckResult(false, null, null, null);
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public DuplicateReason getReason() {
            return reason;
        }

        public FulfillmentOrder getExistingOrder() {
            return existingOrder;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Reasons for duplicate detection.
     */
    public enum DuplicateReason {
        IDEMPOTENCY_KEY("Duplicate idempotency key"),
        SELLER_ORDER_ID("Duplicate seller order ID"),
        FUZZY_MATCH("Similar order detected (same displayable ID, address, and timeframe)");

        private final String description;

        DuplicateReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
