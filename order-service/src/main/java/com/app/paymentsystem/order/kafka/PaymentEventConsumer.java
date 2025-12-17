package com.app.paymentsystem.order.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.order.domain.OrderStatus;
import com.app.paymentsystem.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.PaymentResultEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;
	

    // Listen for payment success 
    // If this throws exception: retry happens automatically 
    // after retries → message goes to payment-result.DLT
    @KafkaListener(topics = "payment-result", groupId = "order-service-group")
    public void consumePaymentResponse(String message) {
    	
    	log.info("Received payment result message → {}", message);

    	try {
    		
    		PaymentResultEvent event = objectMapper.readValue(message, PaymentResultEvent.class);
    		log.error("🔥 Processing payment-result message at {}", System.currentTimeMillis());
    		// Force failure for testing retry/DLQ 
    		if (true) { throw new RuntimeException("FORCE FAILURE"); }
    		
    		
    		if ("SUCCESS".equals(event.getStatus())) {
                orderService.updateOrderStatus(
                        event.getTransactionId(),
                        OrderStatus.PAYMENT_SUCCESS
                );
            } else {
                orderService.updateOrderStatus(
                        event.getTransactionId(),
                        OrderStatus.PAYMENT_FAILED
                );
            }
    		
    	}catch(Exception e) {
            log.error("Failed to process PaymentResultEvent", e);
            throw new RuntimeException(e); // triggers retry + DLQ
    	}
    }
}
