package com.paklog.ordermanagement.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.FulfillmentOrderStatus;
import com.paklog.ordermanagement.domain.model.OrderItem;
import com.paklog.ordermanagement.domain.repository.FulfillmentOrderRepository;

class FulfillmentOrderServiceTest {

    @Mock
    private FulfillmentOrderRepository fulfillmentOrderRepository;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private FulfillmentOrderService fulfillmentOrderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder_Success() {
        // Given
        FulfillmentOrder order = createTestOrder();
        when(fulfillmentOrderRepository.findBySellerFulfillmentOrderId(order.getSellerFulfillmentOrderId()))
                .thenReturn(Optional.empty());
        when(fulfillmentOrderRepository.save(any(FulfillmentOrder.class))).thenReturn(order);

        // When
        FulfillmentOrder createdOrder = fulfillmentOrderService.createOrder(order);

        // Then
        assertNotNull(createdOrder);
        assertEquals(FulfillmentOrderStatus.RECEIVED, createdOrder.getStatus());
        verify(fulfillmentOrderRepository).findBySellerFulfillmentOrderId(order.getSellerFulfillmentOrderId());
        verify(fulfillmentOrderRepository).save(order);
        verify(eventPublisherService).publishEvent(any());
    }

    @Test
    void testCreateOrder_Conflict() {
        // Given
        FulfillmentOrder order = createTestOrder();
        when(fulfillmentOrderRepository.findBySellerFulfillmentOrderId(order.getSellerFulfillmentOrderId()))
                .thenReturn(Optional.of(order));

        // When & Then
        assertThrows(IllegalStateException.class, () -> fulfillmentOrderService.createOrder(order));
        verify(fulfillmentOrderRepository).findBySellerFulfillmentOrderId(order.getSellerFulfillmentOrderId());
        verify(fulfillmentOrderRepository, never()).save(any());
        verify(eventPublisherService, never()).publishEvent(any());
    }

    @Test
    void testGetOrderById_Found() {
        // Given
        UUID orderId = UUID.randomUUID();
        FulfillmentOrder order = createTestOrder();
        order.setOrderId(orderId);
        when(fulfillmentOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        Optional<FulfillmentOrder> result = fulfillmentOrderService.getOrderById(orderId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getOrderId());
        verify(fulfillmentOrderRepository).findById(orderId);
    }

    @Test
    void testGetOrderById_NotFound() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(fulfillmentOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        Optional<FulfillmentOrder> result = fulfillmentOrderService.getOrderById(orderId);

        // Then
        assertFalse(result.isPresent());
        verify(fulfillmentOrderRepository).findById(orderId);
    }

    @Test
    void testCancelOrder_Success() {
        // Given
        UUID orderId = UUID.randomUUID();
        String cancellationReason = "Customer requested cancellation";
        FulfillmentOrder order = createTestOrder();
        order.setOrderId(orderId);
        order.receive(); // Set to received status
        when(fulfillmentOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(fulfillmentOrderRepository.save(any(FulfillmentOrder.class))).thenReturn(order);

        // When
        FulfillmentOrder cancelledOrder = fulfillmentOrderService.cancelOrder(orderId, cancellationReason);

        // Then
        assertNotNull(cancelledOrder);
        assertEquals(FulfillmentOrderStatus.CANCELLED, cancelledOrder.getStatus());
        assertEquals(cancellationReason, cancelledOrder.getCancellationReason());
        verify(fulfillmentOrderRepository).findById(orderId);
        verify(fulfillmentOrderRepository).save(order);
        verify(eventPublisherService).publishEvent(any());
    }

    @Test
    void testCancelOrder_NotFound() {
        // Given
        UUID orderId = UUID.randomUUID();
        String cancellationReason = "Customer requested cancellation";
        when(fulfillmentOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> fulfillmentOrderService.cancelOrder(orderId, cancellationReason));
        verify(fulfillmentOrderRepository).findById(orderId);
        verify(fulfillmentOrderRepository, never()).save(any());
    }

    private FulfillmentOrder createTestOrder() {
        return new FulfillmentOrder(
                UUID.randomUUID(),
                "seller-123",
                "display-123",
                LocalDateTime.now(),
                "Test order",
                "STANDARD",
                createTestAddress(),
                createTestItems()
        );
    }

    private Address createTestAddress() {
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

    private List<OrderItem> createTestItems() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(
                "SKU-123",
                "item-1",
                2,
                "Happy Birthday!",
                "Fragile"
        ));
        return items;
    }
}