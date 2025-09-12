package com.paklog.ordermanagement.interfaces.rest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.ordermanagement.application.service.FulfillmentOrderService;
import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.OrderItem;
import com.paklog.ordermanagement.interfaces.dto.CancelFulfillmentOrderRequest;
import com.paklog.ordermanagement.interfaces.dto.CreateFulfillmentOrderRequest;
import com.paklog.ordermanagement.interfaces.dto.FulfillmentOrderDto;

@WebMvcTest(FulfillmentOrderController.class)
class FulfillmentOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FulfillmentOrderService fulfillmentOrderService;

    private FulfillmentOrder testOrder;
    private FulfillmentOrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        testOrder = createTestOrder();
        testOrderDto = convertToDto(testOrder);
    }

    @Test
    void testCreateFulfillmentOrder_Success() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createTestRequest();
        when(fulfillmentOrderService.createOrder(any(FulfillmentOrder.class))).thenReturn(testOrder);

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(testOrderDto.getOrderId().toString()))
                .andExpect(jsonPath("$.sellerFulfillmentOrderId").value(testOrderDto.getSellerFulfillmentOrderId()));
    }

    @Test
    void testCreateFulfillmentOrder_Conflict() throws Exception {
        // Given
        CreateFulfillmentOrderRequest request = createTestRequest();
        when(fulfillmentOrderService.createOrder(any(FulfillmentOrder.class)))
                .thenThrow(new IllegalStateException("Order with sellerFulfillmentOrderId already exists"));

        // When & Then
        mockMvc.perform(post("/fulfillment_orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetFulfillmentOrderById_Success() throws Exception {
        // Given
        UUID orderId = testOrder.getOrderId();
        when(fulfillmentOrderService.getOrderById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        mockMvc.perform(get("/fulfillment_orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(testOrderDto.getOrderId().toString()));
    }

    @Test
    void testGetFulfillmentOrderById_NotFound() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        when(fulfillmentOrderService.getOrderById(orderId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/fulfillment_orders/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCancelFulfillmentOrder_Success() throws Exception {
        // Given
        UUID orderId = testOrder.getOrderId();
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest("Customer requested cancellation");
        when(fulfillmentOrderService.cancelOrder(orderId, request.getCancellationReason())).thenReturn(testOrder);

        // When & Then
        mockMvc.perform(post("/fulfillment_orders/{orderId}/cancel", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void testCancelFulfillmentOrder_NotFound() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest("Customer requested cancellation");
        when(fulfillmentOrderService.cancelOrder(orderId, request.getCancellationReason()))
                .thenThrow(new IllegalArgumentException("Order not found"));

        // When & Then
        mockMvc.perform(post("/fulfillment_orders/{orderId}/cancel", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    private CreateFulfillmentOrderRequest createTestRequest() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();
        request.setSellerFulfillmentOrderId("seller-123");
        request.setDisplayableOrderId("display-123");
        request.setDisplayableOrderDate(LocalDateTime.now());
        request.setDisplayableOrderComment("Test order");
        request.setShippingSpeedCategory("STANDARD");
        request.setDestinationAddress(createTestAddress());
        request.setItems(createTestItems());
        return request;
    }

    private FulfillmentOrder createTestOrder() {
        FulfillmentOrder order = new FulfillmentOrder(
                UUID.randomUUID(),
                "seller-123",
                "display-123",
                LocalDateTime.now(),
                "Test order",
                "STANDARD",
                createTestAddress(),
                createTestItems()
        );
        order.receive();
        return order;
    }

    private Address createTestAddress() {
        Address address = new Address();
        address.setName("John Doe");
        address.setAddressLine1("123 Main St");
        address.setAddressLine2("Apt 4B");
        address.setCity("New York");
        address.setStateOrRegion("NY");
        address.setPostalCode("10001");
        address.setCountryCode("US");
        return address;
    }

    private List<OrderItem> createTestItems() {
        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setSellerSku("SKU-123");
        item.setSellerFulfillmentOrderItemId("item-1");
        item.setQuantity(2);
        item.setGiftMessage("Happy Birthday!");
        item.setDisplayableComment("Fragile");
        items.add(item);
        return items;
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