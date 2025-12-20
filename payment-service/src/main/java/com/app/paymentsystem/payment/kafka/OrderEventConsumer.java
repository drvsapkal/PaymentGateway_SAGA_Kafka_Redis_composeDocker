package com.app.paymentsystem.payment.kafka;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.OrderCreatedEvent;
import com.saga.events.PaymentRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

	private final PaymentService paymentService;
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";	


    @KafkaListener(topics = "order-created", groupId = "payment-service-group")
    public void consumeOrderCreatedEvent(ConsumerRecord<String, String> record) {
    	log.info("Received OrderCreated event: {}", record.value());
    	
    	
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
    	
    	OrderCreatedEvent event =
    	        new ObjectMapper().readValue(record.value(), OrderCreatedEvent.class);

		PaymentRequestedEvent paymentEvent = new PaymentRequestedEvent(event.getTransactionId(), event.getOrderId(),
				event.getAmount());

		paymentService.processPayment(paymentEvent);
	} catch (Exception e) {
		log.error("Failed to process OrderCreatedEvent", e);
		throw new RuntimeException(e); // triggers retry + DLQ
	} finally {
		MDC.clear();
	}
}
 
}