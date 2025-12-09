package com.app.paymentsystem.payment.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.OrderCreatedEvent;
import com.saga.events.PaymentResultEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    @Autowired
	private final PaymentService paymentService;
    
    @Autowired
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    private static final String PAYMENT_RESULT_TOPIC = "payment-result";
    

    @KafkaListener(topics = "order-created", groupId = "payment-service-group")
    public void consumeOrderCreatedEvent(String message) {
    	log.info("Received OrderCreated event: {}", message);

    	try {
            ObjectMapper mapper = new ObjectMapper();
            OrderCreatedEvent event = mapper.readValue(message, OrderCreatedEvent.class);

            log.info("Received OrderCreatedEvent → {}", event);

            boolean success = paymentService.processPayment(
                    event.getTransactionId(),
                    event.getOrderId(),
                    event.getAmount()
            );

            PaymentResultEvent response = new PaymentResultEvent(
                    event.getTransactionId(),
                    event.getOrderId(),
                    success ? "SUCCESS" : "FAILED"
            );

            kafkaTemplate.send(PAYMENT_RESULT_TOPIC, event.getTransactionId(), mapper.writeValueAsString(response));
            log.info("Published PaymentResultEvent → {}", response);

        } catch (Exception ex) {
            log.error("Error processing OrderCreatedEvent: {}", ex.getMessage());
        }
    }
}