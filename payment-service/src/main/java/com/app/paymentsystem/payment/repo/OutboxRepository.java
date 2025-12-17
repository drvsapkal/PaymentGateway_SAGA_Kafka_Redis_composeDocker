package com.app.paymentsystem.payment.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.paymentsystem.payment.model.OutboxEvent;


public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop10ByStatusOrderByCreatedAtAsc(String status);
}