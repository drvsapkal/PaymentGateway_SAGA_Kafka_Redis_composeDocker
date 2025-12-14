package com.app.paymentsystem.payment.service;

import org.springframework.stereotype.Service;

import com.app.paymentsystem.payment.kafka.PaymentEventProducer;
import com.app.paymentsystem.payment.model.Payment;
import com.app.paymentsystem.payment.repo.PaymentRepository;

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
    public boolean processPayment(String transactionId, Long orderId, Integer amount) {
 
	   log.info("Processing payment for orderId={}, txnId={}", orderId, transactionId);
	       
       // IDEMPOTENCY CHECK
       if(paymentRepository.findByTransactionId(transactionId).isPresent()) {
    	   log.warn("Duplcate payment event ignored for txnId={}",transactionId);
    	   return true;
       } 
       
       
        boolean success = Math.random() > 0.3;   // 70% success reference simulation
        
        Payment payment = Payment.builder()
        				.transactionId(transactionId)
        				.orderId(orderId)
        				.amount(amount)
        				.status(success ? "SUCCESS" : "FAILED")
        				.build();
        
        paymentRepository.save(payment);

//        Publish the payment result via PaymentEventProducer
//        if (success) {
//            paymentEventProducer.sendPaymentSuccess(transactionId, orderId);
//            log.info("Payment SUCCESS for Txn={} → success={}", transactionId, success);
//        } else {
//            paymentEventProducer.sendPaymentFailure(transactionId, orderId);
//            log.warn("Payment FAILED for Txn={} → success={}", transactionId, success);
//        }

        log.info("Payment {} → TxnId={} OrderId={}", success ? "SUCCESS" : "FAILED",
                transactionId,
                orderId);
        
        return success;

    }
    
}