package com.app.paymentsystem.payment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.app.paymentsystem.payment.model.OutboxEvent;
import com.app.paymentsystem.payment.model.Payment;
import com.app.paymentsystem.payment.repo.OutboxRepository;
import com.app.paymentsystem.payment.repo.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.PaymentRequestedEvent;
import com.saga.events.PaymentResultEvent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
	
	private final PaymentRepository paymentRepository;
	private final OutboxRepository outboxRepository; 
	private final ObjectMapper objectMapper;
	

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
       
	    //Perform payment logic
        boolean success = Math.random() > 0.3;   // 70% success reference simulation
        
        Payment payment = Payment.builder()
        				.transactionId(transactionId)
        				.orderId(event.getOrderId())
        				.amount(event.getAmount())
        				.status(success ? "SUCCESS" : "FAILED")
        				.build();
        
        paymentRepository.save(payment);
        
        PaymentResultEvent resultEvent = new PaymentResultEvent(transactionId, payment.getStatus());
        
        try {
            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("PAYMENT")
                    .aggregateId(payment.getTransactionId()) //Required Payment Id here if possible
                    .eventType("PAYMENT_RESULT")
                    .payload(objectMapper.writeValueAsString(resultEvent))
                    .status("NEW")
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(outbox);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payment event", e);
        }
    }
    
}