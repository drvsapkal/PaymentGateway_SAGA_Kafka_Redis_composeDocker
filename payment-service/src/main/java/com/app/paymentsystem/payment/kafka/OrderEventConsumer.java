package com.app.paymentsystem.payment.kafka;

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
        

    @KafkaListener(topics = "order-created", groupId = "payment-service-group")
    public void consumeOrderCreatedEvent(String message) throws Exception{
    	log.info("Received OrderCreated event: {}", message);
    	
    	OrderCreatedEvent event =
    	        new ObjectMapper().readValue(message, OrderCreatedEvent.class);

    	PaymentRequestedEvent paymentEvent =
                new PaymentRequestedEvent(
                        event.getTransactionId(),
                        event.getOrderId(),
                        event.getAmount()
                );

    	paymentService.processPayment(paymentEvent);
    }
 
}