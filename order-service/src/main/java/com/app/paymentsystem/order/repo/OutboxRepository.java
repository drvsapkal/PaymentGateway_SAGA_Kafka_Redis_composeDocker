package com.app.paymentsystem.order.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.paymentsystem.order.entity.OutboxEvent;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop10ByStatusOrderByCreatedAtAsc(String status);
}