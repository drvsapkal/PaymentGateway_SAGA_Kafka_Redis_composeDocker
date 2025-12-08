package com.app.paymentsystem.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.paymentsystem.order.dto.OrderRequest;
import com.app.paymentsystem.order.entity.Order;
import com.app.paymentsystem.order.repo.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	@Autowired
    private final OrderRepository orderRepository;
	@Autowired
    private final RedisTemplate<String, Object> redisTemplate;
	@Autowired
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String ORDER_CREATED_TOPIC = "order-created";

    @Transactional
    public Order createOrder(OrderRequest req) {
        log.info("Creating order for Transaction ID: {}", req.getTransactionId());

        Order order = Order.builder()
                .transactionId(req.getTransactionId())
                .customerId(req.getCustomerId())
                .amount(req.getAmount())
                .status("CREATED")
                .build();

        order = orderRepository.save(order);

        redisTemplate.opsForValue().set("order_" + order.getId(), order);
        log.info("Saved Order {} in Redis Cache", order.getId());
        
        String eventPayload = order.getId() + "," + req.getAmount();
        kafkaTemplate.send(ORDER_CREATED_TOPIC, req.getTransactionId(), eventPayload);
        
        log.info("Published Kafka Event: Topic={}, TxnId={}, Payload={}",
                ORDER_CREATED_TOPIC, req.getTransactionId(), eventPayload);
        
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
    
    public void updateOrderStatus(String transactionId, String status) {
        Order order = orderRepository.findByTransactionId(transactionId);
        if (order != null) {
            order.setStatus(status);
            orderRepository.save(order);
        }
    }
    
}