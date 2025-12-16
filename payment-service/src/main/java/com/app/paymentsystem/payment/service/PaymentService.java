package com.app.paymentsystem.payment.service;

import org.springframework.stereotype.Service;

import com.app.paymentsystem.payment.kafka.PaymentEventProducer;
import com.app.paymentsystem.payment.model.Payment;
import com.app.paymentsystem.payment.repo.PaymentRepository;
import com.saga.events.PaymentRequestedEvent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
	
	private final PaymentRepository paymentRepository;
	private final PaymentEventProducer paymentEventProducer;

	@Transactional
    public void processPayment(PaymentRequestedEvent event) {
 
	   log.info("Processing payment for orderId={}, txnId={}", event.getOrderId(), event.getTransactionId());
	   
	   String transactionId = event.getTransactionId();
	       
	   //  IDEMPOTENCY CHECK
	    if (paymentRepository.existsByTransactionId(transactionId)) {
	        log.warn("Duplicate payment event ignored for txnId={}", transactionId);
	        return;
	    }
	    
	    log.info("Processing payment txnId={} orderId={}",
	            transactionId, event.getOrderId());
       
        boolean success = Math.random() > 0.3;   // 70% success reference simulation
        
        Payment payment = Payment.builder()
        				.transactionId(transactionId)
        				.orderId(event.getOrderId())
        				.amount(event.getAmount())
        				.status(success ? "SUCCESS" : "FAILED")
        				.build();
        
        paymentRepository.save(payment);

//        Publish the payment result via PaymentEventProducer
        if (success) {
            paymentEventProducer.sendPaymentSuccess(transactionId);
            log.info("Payment SUCCESS for Txn={}", transactionId);
        } else {
            paymentEventProducer.sendPaymentFailure(transactionId);
            log.warn("Payment FAILED for Txn={}", transactionId);
        }

    }
    
}