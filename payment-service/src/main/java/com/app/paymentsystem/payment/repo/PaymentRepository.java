package com.app.paymentsystem.payment.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.paymentsystem.payment.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
}
