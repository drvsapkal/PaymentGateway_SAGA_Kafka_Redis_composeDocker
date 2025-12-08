package com.app.paymentsystem.order.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.paymentsystem.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByTransactionId(String transactionId);
}
