package com.app.paymentsystem.order.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.paymentsystem.order.client.InventoryClient;
import com.app.paymentsystem.order.domain.OrderStatus;
import com.app.paymentsystem.order.dto.OrderRequest;
import com.app.paymentsystem.order.entity.Order;
import com.app.paymentsystem.order.entity.OutboxEvent;
import com.app.paymentsystem.order.repo.OrderRepository;
import com.app.paymentsystem.order.repo.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.OrderCreatedEvent;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    
    private final OutboxRepository outboxRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    
    private final ObjectMapper objectMapper;
    
    private final InventoryClient inventoryClient;
	
    private static final String ORDER_CREATED_TOPIC = "Order created";

    @Transactional
    public Order createOrder(OrderRequest req, String idempotencyKey) {
        log.info("Creating order for Customer ID: {}", req.getCustomerId());
        
        // 1 Idempotency check (no duplicate order create)
        Optional<Order> existing =
                orderRepository.findByIdempotencyKey(idempotencyKey);
        
        if (existing.isPresent()) {
            log.info("Duplicate request detected → idempotencyKey={}", idempotencyKey);
            return existing.get();
        }
        
      // 2️ Generate transactionId internally (to track particular  order everywhere)this is saga ID
        String transactionId = UUID.randomUUID().toString();

        Order order = Order.builder()
                .transactionId(transactionId)
                .idempotencyKey(idempotencyKey)
                .customerId(req.getCustomerId())
                .amount(req.getAmount())
                .status(OrderStatus.CREATED)
                .build();

        order = orderRepository.save(order);
        
      // IMMEDIATE INVENTORY CALL (SYNC)
        boolean reserved = reserveInventory(req.getProductId(), req.getQuantity()); 
        
        if (!reserved) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw new RuntimeException("Out of stock");
        }
        
      // Inventory success → allow payment
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(order);

      //4
   	 	redisTemplate.opsForValue().set("order_" + order.getId(), order);
        log.info("Saved Order {} in Redis Cache", order.getId());
        
      //3 CREATE OUTBOX EVENT (SAME TX)
        
        try {
        
        	OrderCreatedEvent event = new OrderCreatedEvent(transactionId, order.getId(), order.getAmount());
        	
        	OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("ORDER")
                    .aggregateId(transactionId)
                    .eventType("ORDER_CREATED")
                    .payload(objectMapper.writeValueAsString(event))
                    .status("PENDING")
                    .correlationId(MDC.get("X-Correlation-Id"))
                    .createdAt(LocalDateTime.now())
                    .build();
        	
        	outboxRepository.save(outbox);
      
             
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        
        log.info(ORDER_CREATED_TOPIC+" successfully ID={} Txn={}", order.getId(), order.getTransactionId());
       
        
        return order;
    }

    public Order getOrder(Long id) {
        String cacheKey = "order_" + id;

        Object cachedOrder = redisTemplate.opsForValue().get(cacheKey);
        if (cachedOrder != null) {
            log.info("Returning cached order {}", cachedOrder);
            return (Order) cachedOrder;
        }

        return orderRepository.findById(id)
                .orElse(null);
    }
    
    @Transactional
    public void updateOrderStatus(String transactionId, OrderStatus  status) {
        Order order = orderRepository.findByTransactionId(transactionId)
        			.orElseThrow(() -> new IllegalStateException("Oder not found"));
        
        //IDEMPOTENCY CHECK
        if (order.getStatus() == status) {
            log.warn("Duplicate order update ignored for txnId={}", transactionId);
            return;
        }
     // 1️⃣ Update order state
        order.setStatus(status);
        orderRepository.save(order);	// Optimistic lock happens here
        
     // 2️⃣ CACHE EVICTION (MANDATORY)
        redisTemplate.delete("order_" + order.getId());
        
     // 3 Compensation actions
//        releaseInventory(order);
//        cancelShipment(order);
        
        log.info("Order {} updated to status={}", transactionId, status);
    }
  
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    	public boolean reserveInventory(Long productId, int quantity) {
    	    return inventoryClient.reserve(productId, quantity);
    	}

    public boolean inventoryFallback(
            Long productId,
            int quantity,
            Throwable ex) {

        log.error("Inventory service unavailable", ex);

        // Business decision:
        return false; // treat as OUT OF STOCK
    }

}