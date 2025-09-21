package com.paklog.ordermanagement.config;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.infrastructure.repository.mongodb.MongoFulfillmentOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseSeederTest {

    @Mock
    private MongoFulfillmentOrderRepository fulfillmentOrderRepository;

    @Captor
    private ArgumentCaptor<FulfillmentOrder> orderCaptor;

    private DatabaseSeeder databaseSeeder;

    @BeforeEach
    void setUp() {
        databaseSeeder = new DatabaseSeeder(fulfillmentOrderRepository);
    }

    @Test
    void testConstructor() {
        assertNotNull(databaseSeeder);
    }

    @Test
    void testRunWhenDatabaseIsEmpty() throws Exception {
        // Given
        when(fulfillmentOrderRepository.count()).thenReturn(0L);
        when(fulfillmentOrderRepository.saveOrder(any(FulfillmentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        databaseSeeder.run();

        // Then
        verify(fulfillmentOrderRepository).count();
        verify(fulfillmentOrderRepository, times(100)).saveOrder(orderCaptor.capture());

        // Verify that 100 orders were created
        assertEquals(100, orderCaptor.getAllValues().size());

        // Verify the structure of created orders
        FulfillmentOrder firstOrder = orderCaptor.getAllValues().get(0);
        assertNotNull(firstOrder);
        assertNotNull(firstOrder.getOrderId());
        assertNotNull(firstOrder.getSellerFulfillmentOrderId());
        assertNotNull(firstOrder.getDisplayableOrderId());
        assertNotNull(firstOrder.getDisplayableOrderDate());
        assertNotNull(firstOrder.getDisplayableOrderComment());
        assertNotNull(firstOrder.getShippingSpeedCategory());
        assertNotNull(firstOrder.getDestinationAddress());
        assertNotNull(firstOrder.getItems());
        assertFalse(firstOrder.getItems().isEmpty());
    }

    @Test
    void testRunWhenDatabaseHasData() throws Exception {
        // Given
        when(fulfillmentOrderRepository.count()).thenReturn(50L);

        // When
        databaseSeeder.run();

        // Then
        verify(fulfillmentOrderRepository).count();
        verify(fulfillmentOrderRepository, never()).saveOrder(any(FulfillmentOrder.class));
    }

    @Test
    void testRunWhenDatabaseHasOneRecord() throws Exception {
        // Given
        when(fulfillmentOrderRepository.count()).thenReturn(1L);

        // When
        databaseSeeder.run();

        // Then
        verify(fulfillmentOrderRepository).count();
        verify(fulfillmentOrderRepository, never()).saveOrder(any(FulfillmentOrder.class));
    }

    @Test
    void testCreateFakeOrderStructure() throws Exception {
        // Given
        when(fulfillmentOrderRepository.count()).thenReturn(0L);
        when(fulfillmentOrderRepository.saveOrder(any(FulfillmentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        databaseSeeder.run();

        // Then
        verify(fulfillmentOrderRepository, times(100)).saveOrder(orderCaptor.capture());

        FulfillmentOrder order = orderCaptor.getAllValues().get(0);

        // Verify order structure
        assertNotNull(order.getOrderId());
        assertTrue(order.getSellerFulfillmentOrderId().startsWith("seller-order-"));
        assertTrue(order.getDisplayableOrderId().startsWith("display-order-"));
        assertNotNull(order.getDisplayableOrderDate());
        assertEquals("STANDARD", order.getShippingSpeedCategory());

        // Verify address structure
        assertNotNull(order.getDestinationAddress());
        assertNotNull(order.getDestinationAddress().getName());
        assertNotNull(order.getDestinationAddress().getAddressLine1());
        assertNotNull(order.getDestinationAddress().getAddressLine2());
        assertNotNull(order.getDestinationAddress().getCity());
        assertNotNull(order.getDestinationAddress().getStateOrRegion());
        assertNotNull(order.getDestinationAddress().getPostalCode());
        assertEquals("US", order.getDestinationAddress().getCountryCode());

        // Verify items structure
        assertNotNull(order.getItems());
        assertTrue(order.getItems().size() >= 1);
        assertTrue(order.getItems().size() <= 4);

        order.getItems().forEach(item -> {
            assertNotNull(item.getSellerSku());
            assertTrue(item.getSellerFulfillmentOrderItemId().startsWith("item-"));
            assertNotNull(item.getQuantity());
            assertTrue(item.getQuantity() >= 1);
            assertTrue(item.getQuantity() <= 10);
            assertNotNull(item.getGiftMessage());
            assertNotNull(item.getDisplayableComment());
        });
    }

    @Test
    void testMultipleRunsWithEmptyDatabase() throws Exception {
        // Given
        when(fulfillmentOrderRepository.count()).thenReturn(0L);
        when(fulfillmentOrderRepository.saveOrder(any(FulfillmentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        databaseSeeder.run();
        databaseSeeder.run();

        // Then
        verify(fulfillmentOrderRepository, times(2)).count();
        verify(fulfillmentOrderRepository, times(200)).saveOrder(any(FulfillmentOrder.class));
    }

    @Test
    void testRunWithException() throws Exception {
        // Given
        when(fulfillmentOrderRepository.count()).thenReturn(0L);
        when(fulfillmentOrderRepository.saveOrder(any(FulfillmentOrder.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> databaseSeeder.run());

        verify(fulfillmentOrderRepository).count();
        verify(fulfillmentOrderRepository, atLeastOnce()).saveOrder(any(FulfillmentOrder.class));
    }

    @Test
    void testOrdersHaveUniqueIds() throws Exception {
        // Given
        when(fulfillmentOrderRepository.count()).thenReturn(0L);
        when(fulfillmentOrderRepository.saveOrder(any(FulfillmentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        databaseSeeder.run();

        // Then
        verify(fulfillmentOrderRepository, times(100)).saveOrder(orderCaptor.capture());

        var orders = orderCaptor.getAllValues();
        var orderIds = orders.stream()
                .map(FulfillmentOrder::getOrderId)
                .distinct()
                .count();

        assertEquals(100, orderIds); // All order IDs should be unique

        var sellerOrderIds = orders.stream()
                .map(FulfillmentOrder::getSellerFulfillmentOrderId)
                .distinct()
                .count();

        assertEquals(100, sellerOrderIds); // All seller order IDs should be unique
    }

    @Test
    void testOrderDatesAreInPast() throws Exception {
        // Given
        when(fulfillmentOrderRepository.count()).thenReturn(0L);
        when(fulfillmentOrderRepository.saveOrder(any(FulfillmentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        databaseSeeder.run();

        // Then
        verify(fulfillmentOrderRepository, times(100)).saveOrder(orderCaptor.capture());

        var orders = orderCaptor.getAllValues();
        orders.forEach(order -> {
            assertNotNull(order.getDisplayableOrderDate());
            assertTrue(order.getDisplayableOrderDate().isBefore(java.time.LocalDateTime.now()));
            assertTrue(order.getDisplayableOrderDate().isAfter(
                java.time.LocalDateTime.now().minusDays(31))); // Within last 30 days
        });
    }

    @Test
    void testRunWithZeroCountFirst() throws Exception {
        // Given
        when(fulfillmentOrderRepository.count()).thenReturn(0L);
        when(fulfillmentOrderRepository.saveOrder(any(FulfillmentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        databaseSeeder.run("--force"); // Test with arguments

        // Then
        verify(fulfillmentOrderRepository).count();
        verify(fulfillmentOrderRepository, times(100)).saveOrder(any(FulfillmentOrder.class));
    }
}