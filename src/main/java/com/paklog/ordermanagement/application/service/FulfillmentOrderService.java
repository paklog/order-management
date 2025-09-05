package com.paklog.ordermanagement.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paklog.ordermanagement.domain.event.FulfillmentOrderReceivedEvent;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.repository.FulfillmentOrderRepository;

@Service
public class FulfillmentOrderService {
    
    private final FulfillmentOrderRepository fulfillmentOrderRepository;
    private final EventPublisherService eventPublisherService;
    
    public FulfillmentOrderService(FulfillmentOrderRepository fulfillmentOrderRepository,
                                  EventPublisherService eventPublisherService) {
        this.fulfillmentOrderRepository = fulfillmentOrderRepository;
        this.eventPublisherService = eventPublisherService;
    }
    
    @Transactional
    public FulfillmentOrder createOrder(FulfillmentOrder order) {
        // Check if order with same sellerFulfillmentOrderId already exists
        Optional<FulfillmentOrder> existingOrder = 
            fulfillmentOrderRepository.findBySellerFulfillmentOrderId(order.getSellerFulfillmentOrderId());
        
        if (existingOrder.isPresent()) {
            throw new IllegalStateException("Order with sellerFulfillmentOrderId already exists");
        }
        
        // Receive the order
        order.receive();
        
        // Save the order
        FulfillmentOrder savedOrder = fulfillmentOrderRepository.save(order);
        
        // Publish event
        FulfillmentOrderReceivedEvent event = new FulfillmentOrderReceivedEvent(savedOrder);
        eventPublisherService.publishEvent(event);
        
        return savedOrder;
    }
    
    public Optional<FulfillmentOrder> getOrderById(UUID orderId) {
        return fulfillmentOrderRepository.findById(orderId);
    }
    
    @Transactional
    public FulfillmentOrder cancelOrder(UUID orderId) {
        FulfillmentOrder order = fulfillmentOrderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.cancel();
        FulfillmentOrder savedOrder = fulfillmentOrderRepository.save(order);
        
        // TODO: Publish cancellation event
        
        return savedOrder;
    }
}