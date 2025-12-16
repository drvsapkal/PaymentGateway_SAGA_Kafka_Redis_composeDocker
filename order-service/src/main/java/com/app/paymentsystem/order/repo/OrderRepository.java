package com.app.paymentsystem.order.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.paymentsystem.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
	
    Optional<Order> findByIdempotencyKey(String idempotencyKey);

	Optional<Order> findByTransactionId(String transactionId);

}
