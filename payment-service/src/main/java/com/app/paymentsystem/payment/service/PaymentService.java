package com.app.paymentsystem.payment.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {

    public boolean processPayment(String transactionId, Long orderId, Integer amount) {

        log.info("Processing Payment: TxnId={} OrderId={} Amount={}", transactionId, orderId, amount);

        boolean isSuccess = Math.random() > 0.3;   // 70% success reference simulation

        if (isSuccess) {
            log.info("Payment SUCCESS for TxnId={} OrderId={}", transactionId, orderId);
        } else {
            log.error("Payment FAILED for TxnId={} OrderId={}", transactionId, orderId);
        }

        return isSuccess;
    }
    
}