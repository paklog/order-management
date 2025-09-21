package com.paklog.ordermanagement.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.FulfillmentOrderStatus;
import com.paklog.ordermanagement.domain.model.OrderItem;
import com.paklog.ordermanagement.domain.repository.FulfillmentOrderRepository;

@ExtendWith(MockitoExtension.class)
class FulfillmentOrderServiceTest {

    @Mock
    private FulfillmentOrderRepository fulfillmentOrderRepository;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private FulfillmentOrderService fulfillmentOrderService;

    private FulfillmentOrder order;

    @BeforeEach
    void setUp() {
        order = buildOrder("seller-id-1", "key-1");
    }

    @Test
    void createOrderShouldPersistWhenIdempotencyKeyIsNew() {
        when(fulfillmentOrderRepository.findByIdempotencyKey(order.getIdempotencyKey()))
            .thenReturn(Optional.empty());
        when(fulfillmentOrderRepository.findBySellerFulfillmentOrderId(order.getSellerFulfillmentOrderId()))
            .thenReturn(Optional.empty());
        when(fulfillmentOrderRepository.saveOrder(order)).thenReturn(order);

        FulfillmentOrder created = fulfillmentOrderService.createOrder(order);

        assertThat(created.getStatus()).isEqualTo(FulfillmentOrderStatus.RECEIVED);
        verify(fulfillmentOrderRepository).saveOrder(order);
        verify(eventPublisherService).publishEvent(any());
    }

    @Test
    void createOrderShouldReturnExistingOrderWhenIdempotencyKeyMatches() {
        FulfillmentOrder existing = buildOrder("seller-id-2", order.getIdempotencyKey());
        existing.receive();
        when(fulfillmentOrderRepository.findByIdempotencyKey(order.getIdempotencyKey()))
            .thenReturn(Optional.of(existing));

        FulfillmentOrder result = fulfillmentOrderService.createOrder(order);

        assertThat(result).isSameAs(existing);
        verify(fulfillmentOrderRepository, never()).findBySellerFulfillmentOrderId(anyString());
        verify(fulfillmentOrderRepository, never()).saveOrder(any());
        verifyNoInteractions(eventPublisherService);
    }

    @Test
    void createOrderShouldFailWhenSellerFulfillmentOrderIdExists() {
        when(fulfillmentOrderRepository.findByIdempotencyKey(order.getIdempotencyKey()))
            .thenReturn(Optional.empty());
        when(fulfillmentOrderRepository.findBySellerFulfillmentOrderId(order.getSellerFulfillmentOrderId()))
            .thenReturn(Optional.of(buildOrder(order.getSellerFulfillmentOrderId(), "other-key")));

        assertThrows(IllegalStateException.class, () -> fulfillmentOrderService.createOrder(order));
        verify(fulfillmentOrderRepository, never()).saveOrder(any());
        verifyNoInteractions(eventPublisherService);
    }

    private FulfillmentOrder buildOrder(String sellerId, String idempotencyKey) {
        return new FulfillmentOrder(
            UUID.randomUUID(),
            sellerId,
            "display-id-" + UUID.randomUUID(),
            LocalDateTime.now(),
            "comment",
            "STANDARD",
            new Address(
                "John Doe",
                "123 Main St",
                "Apt 4",
                "Metropolis",
                "NY",
                "12345",
                "US"
            ),
            List.of(new OrderItem("sku-1", "item-1", 1, "msg", "comment")),
            idempotencyKey
        );
    }
}
