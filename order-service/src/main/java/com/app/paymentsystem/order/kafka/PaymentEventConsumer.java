package com.app.paymentsystem.order.kafka;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.order.domain.OrderStatus;
import com.app.paymentsystem.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.PaymentResultEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("docker")
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";	

    // Listen for payment success 
    // If this throws exception: retry happens automatically 
    // after retries → message goes to payment-result.DLT
    @KafkaListener(topics = "payment-result", groupId = "order-service-group")
    public void consumePaymentResponse(ConsumerRecord<String, String> record) {
    	
    	log.info("Received payment result message → {}", record.value());

    	String correlationId = null;
    	try {
	    	if (record.headers().lastHeader(CORRELATION_ID_HEADER) != null) {
	            correlationId = new String(
	                    record.headers()
	                          .lastHeader(CORRELATION_ID_HEADER)
	                          .value(),
	                    StandardCharsets.UTF_8
	            );
	            MDC.put(CORRELATION_ID_HEADER, correlationId);
	        } 	
    		
    		PaymentResultEvent event = objectMapper.readValue(record.value(), PaymentResultEvent.class);
    		
//    		log.error("Processing payment-result message at {}", System.currentTimeMillis());
//    		// Force failure for testing retry/DLQ 
//    		if (true) { throw new RuntimeException("FORCE FAILURE"); }
//    		
    		
    		if ("SUCCESS".equals(event.getStatus())) {
                orderService.updateOrderStatus(
                        event.getTransactionId(),
                        OrderStatus.PAYMENT_SUCCESS
                );
                
                //Saga Compensation
            } else {
                orderService.updateOrderStatus(
                        event.getTransactionId(),
                        OrderStatus.PAYMENT_FAILED
                );
            }
    		
    	}catch(Exception e) {
            log.error("Failed to process PaymentResultEvent", e);
            throw new RuntimeException(e); // triggers retry + DLQ
    	}finally {
            MDC.clear();
        }
    }
}
