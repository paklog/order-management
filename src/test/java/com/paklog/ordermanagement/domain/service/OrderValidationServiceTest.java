package com.paklog.ordermanagement.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.paklog.ordermanagement.domain.config.OrderValidationConfig;
import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.OrderItem;

@DisplayName("OrderValidationService Tests")
class OrderValidationServiceTest {

    private OrderValidationService validationService;
    private OrderValidationConfig config;

    @BeforeEach
    void setUp() {
        config = new OrderValidationConfig();
        // Use default configuration values
        config.setMaxTotalQuantity(100000);
        config.setMaxItemsPerOrder(100);
        config.setRejectDuplicateSkus(true);
        config.setCheckInventoryAvailability(false);
        config.setCheckProductCatalog(false);
        config.setEnableOrderValueValidation(false);

        validationService = new OrderValidationService(config, null, null);
    }

    @Test
    @DisplayName("Should validate order successfully when all rules pass")
    void shouldValidateSuccessfully() {
        // Given
        FulfillmentOrder order = createValidOrder();

        // When
        OrderValidationService.ValidationResult result = validationService.validate(order);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when items list is empty")
    void shouldFailWhenItemsEmpty() {
        // Given
        FulfillmentOrder order = createValidOrder();
        order.setItems(new ArrayList<>());

        // When
        OrderValidationService.ValidationResult result = validationService.validate(order);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Order must contain at least one item");
    }

    @Test
    @DisplayName("Should fail validation when duplicate SKUs are present")
    void shouldFailWhenDuplicateSkus() {
        // Given
        FulfillmentOrder order = createValidOrder();
        List<OrderItem> items = new ArrayList<>();
        items.add(createOrderItem("SKU-001", 5));
        items.add(createOrderItem("SKU-002", 3));
        items.add(createOrderItem("SKU-001", 2)); // Duplicate
        order.setItems(items);

        // When
        OrderValidationService.ValidationResult result = validationService.validate(order);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Duplicate SKUs found"));
        assertThat(result.getErrors()).anyMatch(error -> error.contains("SKU-001"));
    }

    @Test
    @DisplayName("Should fail validation when total quantity exceeds maximum")
    void shouldFailWhenTotalQuantityExceedsMax() {
        // Given
        FulfillmentOrder order = createValidOrder();
        List<OrderItem> items = new ArrayList<>();
        items.add(createOrderItem("SKU-001", 50000));
        items.add(createOrderItem("SKU-002", 50001));
        order.setItems(items);

        // When
        OrderValidationService.ValidationResult result = validationService.validate(order);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Total order quantity"));
        assertThat(result.getErrors()).anyMatch(error -> error.contains("exceeds maximum allowed (100000)"));
    }

    @Test
    @DisplayName("Should fail validation when total quantity is zero")
    void shouldFailWhenTotalQuantityIsZero() {
        // Given
        FulfillmentOrder order = createValidOrder();
        List<OrderItem> items = new ArrayList<>();
        OrderItem item = createOrderItem("SKU-001", 0);
        items.add(item);
        order.setItems(items);

        // When
        OrderValidationService.ValidationResult result = validationService.validate(order);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Total order quantity must be greater than 0");
    }

    @Test
    @DisplayName("Should fail validation when shipping speed category is invalid")
    void shouldFailWhenInvalidShippingSpeedCategory() {
        // Given
        FulfillmentOrder order = createValidOrder();
        order.setShippingSpeedCategory("INVALID_CATEGORY");

        // When
        OrderValidationService.ValidationResult result = validationService.validate(order);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Invalid shipping speed category"));
    }

    @Test
    @DisplayName("Should fail validation when shipping speed category is blank")
    void shouldFailWhenShippingSpeedCategoryBlank() {
        // Given
        FulfillmentOrder order = createValidOrder();
        order.setShippingSpeedCategory("");

        // When
        OrderValidationService.ValidationResult result = validationService.validate(order);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Shipping speed category is required");
    }

    @Test
    @DisplayName("Should validate all valid shipping speed categories")
    void shouldValidateAllValidShippingCategories() {
        String[] validCategories = {"STANDARD", "EXPEDITED", "PRIORITY", "SAME_DAY", "NEXT_DAY", "SCHEDULED"};

        for (String category : validCategories) {
            // Given
            FulfillmentOrder order = createValidOrder();
            order.setShippingSpeedCategory(category);

            // When
            OrderValidationService.ValidationResult result = validationService.validate(order);

            // Then
            assertThat(result.isValid())
                .withFailMessage("Category %s should be valid", category)
                .isTrue();
        }
    }

    @Test
    @DisplayName("Should handle case-insensitive shipping speed categories")
    void shouldHandleCaseInsensitiveShippingCategories() {
        // Given
        FulfillmentOrder order = createValidOrder();
        order.setShippingSpeedCategory("standard"); // lowercase

        // When
        OrderValidationService.ValidationResult result = validationService.validate(order);

        // Then
        assertThat(result.isValid()).isTrue();
    }

    // Helper methods

    private FulfillmentOrder createValidOrder() {
        Address address = new Address(
            "John Doe",
            "123 Main St",
            "Apt 4B",
            "New York",
            "NY",
            "10001",
            "US"
        );

        List<OrderItem> items = new ArrayList<>();
        items.add(createOrderItem("SKU-001", 5));
        items.add(createOrderItem("SKU-002", 3));

        return new FulfillmentOrder(
            UUID.randomUUID(),
            "SELLER-ORDER-123",
            "ORDER-456",
            LocalDateTime.now(),
            "Thank you for your order",
            "STANDARD",
            address,
            items,
            "idempotency-key-123"
        );
    }

    private OrderItem createOrderItem(String sku, int quantity) {
        return new OrderItem(
            sku,
            "ITEM-" + sku,
            quantity,
            null,
            null
        );
    }
}
