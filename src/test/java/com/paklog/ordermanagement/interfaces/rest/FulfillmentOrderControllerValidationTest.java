package com.paklog.ordermanagement.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.ordermanagement.application.service.EventPublisherService;
import com.paklog.ordermanagement.application.service.FulfillmentOrderService;
import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.FulfillmentPolicy;
import com.paklog.ordermanagement.domain.model.OrderItem;
import com.paklog.ordermanagement.domain.service.OrderValidationService;
import com.paklog.ordermanagement.interfaces.dto.CreateFulfillmentOrderRequest;

@WebMvcTest(controllers = FulfillmentOrderController.class,
    includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = GlobalExceptionHandler.class
    ))
@DisplayName("FulfillmentOrderController Validation Tests")
class FulfillmentOrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FulfillmentOrderService fulfillmentOrderService;

    @MockBean
    private OrderValidationService orderValidationService;

    @MockBean
    private EventPublisherService eventPublisherService;

    @Test
    @DisplayName("Should reject request when Idempotency-Key header is missing")
    void shouldRejectWhenIdempotencyKeyMissing() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should reject request when seller_fulfillment_order_id is blank")
    void shouldRejectWhenSellerOrderIdBlank() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        request.setSellerFulfillmentOrderId("");

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should reject request when displayable_order_id is blank")
    void shouldRejectWhenDisplayableOrderIdBlank() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        request.setDisplayableOrderId("");

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should reject request when displayable_order_date is null")
    void shouldRejectWhenOrderDateNull() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        request.setDisplayableOrderDate(null);

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should reject request when displayable_order_date is in the future")
    void shouldRejectWhenOrderDateInFuture() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        request.setDisplayableOrderDate(LocalDateTime.now().plusDays(1));

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should reject request when shipping_speed_category is blank")
    void shouldRejectWhenShippingSpeedBlank() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        request.setShippingSpeedCategory("");

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should reject request when destination_address is null")
    void shouldRejectWhenAddressNull() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        request.setDestinationAddress(null);

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should reject request when items list is empty")
    void shouldRejectWhenItemsEmpty() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        request.setItems(new ArrayList<>());

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should reject request when address fields are invalid")
    void shouldRejectWhenAddressFieldsInvalid() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        Address invalidAddress = new Address(
            "", // Empty name
            "", // Empty address line 1
            null,
            "", // Empty city
            "", // Empty state
            "INVALID*&^", // Invalid postal code
            "USA" // Invalid country code (should be 2 chars)
        );
        request.setDestinationAddress(invalidAddress);

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should reject request when item quantity is less than 1")
    void shouldRejectWhenItemQuantityInvalid() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("SKU-001", "ITEM-001", 0, null, null)); // Invalid quantity
        request.setItems(items);

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(fulfillmentOrderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("Should accept valid request")
    void shouldAcceptValidRequest() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createValidRequest();
        FulfillmentOrder createdOrder = createMockOrder();

        when(orderValidationService.validate(any()))
            .thenReturn(OrderValidationService.ValidationResult.success());
        when(orderValidationService.checkInventoryAvailability(any()))
            .thenReturn(OrderValidationService.InventoryAvailabilityResult.allAvailable());
        when(fulfillmentOrderService.createOrder(any())).thenReturn(createdOrder);

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .header("Idempotency-Key", "key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted());

        verify(fulfillmentOrderService).createOrder(any());
    }

    // Helper methods

    private CreateFulfillmentOrderRequest createValidRequest() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();
        request.setSellerFulfillmentOrderId("SELLER-123");
        request.setDisplayableOrderId("ORDER-456");
        request.setDisplayableOrderDate(LocalDateTime.now());
        request.setDisplayableOrderComment("Thank you");
        request.setShippingSpeedCategory("STANDARD");
        request.setDestinationAddress(createValidAddress());
        request.setItems(createValidItems());
        request.setFulfillmentPolicy(FulfillmentPolicy.FILL_ALL_AVAILABLE);
        return request;
    }

    private Address createValidAddress() {
        return new Address(
            "John Doe",
            "123 Main St",
            "Apt 4B",
            "New York",
            "NY",
            "10001",
            "US"
        );
    }

    private List<OrderItem> createValidItems() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("SKU-001", "ITEM-001", 5, null, null));
        items.add(new OrderItem("SKU-002", "ITEM-002", 3, null, null));
        return items;
    }

    private FulfillmentOrder createMockOrder() {
        return new FulfillmentOrder(
            UUID.randomUUID(),
            "SELLER-123",
            "ORDER-456",
            LocalDateTime.now(),
            "Thank you",
            "STANDARD",
            createValidAddress(),
            createValidItems(),
            "key-123"
        );
    }
}
