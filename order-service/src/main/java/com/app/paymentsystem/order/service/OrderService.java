package com.app.paymentsystem.order.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.paymentsystem.order.domain.OrderStatus;
import com.app.paymentsystem.order.dto.OrderRequest;
import com.app.paymentsystem.order.entity.Order;
import com.app.paymentsystem.order.kafka.OrderEventProducer;
import com.app.paymentsystem.order.repo.OrderRepository;
import com.saga.events.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final OrderEventProducer orderEventProducer;
	
    private static final String ORDER_CREATED_TOPIC = "Order created";

    @Transactional
    public Order createOrder(OrderRequest req, String idempotencyKey) {
        log.info("Creating order for Customer ID: {}", req.getCustomerId());
        
        // 1 Idempotency check
        Optional<Order> existing =
                orderRepository.findByIdempotencyKey(idempotencyKey);
        
        if (existing.isPresent()) {
            log.info("Duplicate request detected → idempotencyKey={}", idempotencyKey);
            return existing.get();
        }
        
      // 2️ Generate transactionId internally
        String transactionId = UUID.randomUUID().toString();

        Order order = Order.builder()
                .transactionId(transactionId)
                .idempotencyKey(idempotencyKey)
                .customerId(req.getCustomerId())
                .amount(req.getAmount())
                .status(OrderStatus.CREATED)
                .build();

        order = orderRepository.save(order);

        redisTemplate.opsForValue().set("order_" + order.getId(), order);
        log.info("Saved Order {} in Redis Cache", order.getId());

        // 4️ Publish saga event
        OrderCreatedEvent orderEventCreated = new OrderCreatedEvent(
                order.getTransactionId(),
                order.getId(),
                order.getAmount()
        );
        
        orderEventProducer.sendOrderCreatedEvent(orderEventCreated);
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
        
        order.setStatus(status);
        orderRepository.save(order);
        
        log.info("Order {} updated to status={}", transactionId, status);
    }
    
}