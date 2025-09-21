package com.paklog.ordermanagement.interfaces.rest;

import java.util.UUID;

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
    
    private final FulfillmentOrderService fulfillmentOrderService;
    
    public FulfillmentOrderController(FulfillmentOrderService fulfillmentOrderService) {
        this.fulfillmentOrderService = fulfillmentOrderService;
    }
    
    @PostMapping
    public ResponseEntity<FulfillmentOrderDto> createFulfillmentOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateFulfillmentOrderRequest request) {
        try {
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            // Convert request to domain model
            FulfillmentOrder order = convertToDomain(request, idempotencyKey);
            
            // Create the order
            FulfillmentOrder createdOrder = fulfillmentOrderService.createOrder(order);
            
            // Convert to DTO and return
            FulfillmentOrderDto dto = convertToDto(createdOrder);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/{order_id}")
    public ResponseEntity<FulfillmentOrderDto> getFulfillmentOrderById(@PathVariable("order_id") UUID orderId) {
        return fulfillmentOrderService.getOrderById(orderId)
            .map(this::convertToDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{order_id}/cancel")
    public ResponseEntity<Void> cancelFulfillmentOrder(@PathVariable("order_id") UUID orderId, 
                                                      @RequestBody CancelFulfillmentOrderRequest request) {
        try {
            fulfillmentOrderService.cancelOrder(orderId, request.getCancellationReason());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
