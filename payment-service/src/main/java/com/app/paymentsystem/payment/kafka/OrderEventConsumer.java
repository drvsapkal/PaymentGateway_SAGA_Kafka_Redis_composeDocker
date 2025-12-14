package com.app.paymentsystem.payment.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

	private final PaymentService paymentService;
        

    @KafkaListener(topics = "order-created", groupId = "payment-service-group")
    public void consumeOrderCreatedEvent(String message) {
    	log.info("Received OrderCreated event: {}", message);

    	try {
            ObjectMapper mapper = new ObjectMapper();
            OrderCreatedEvent event = mapper.readValue(message, OrderCreatedEvent.class);

            log.info("Received OrderCreatedEvent → {}", event);

            paymentService.processPayment(
                    event.getTransactionId(),
                    event.getOrderId(),
                    event.getAmount()
            );

        } catch (Exception ex) {
            log.error("Error processing OrderCreatedEvent: {}", ex.getMessage());
        }
    }
}