package com.app.paymentsystem.payment.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.paymentsystem.payment.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByTransactionId(String transactionId);
}
