package com.app.paymentsystem.order.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
	
	private final ObjectMapper mapper = new ObjectMapper();

    // Listen for payment success
    @KafkaListener(topics = "payment-result", groupId = "order-service-group")
    public void consumePaymentResponse(String message) {
    	
    	log.info("Received payment result message → {}", message);

    	try {
            PaymentResultEvent event = mapper.readValue(message, PaymentResultEvent.class);

            log.info("Received PaymentResult event: {}", event);

            orderService.updateOrderStatus(event.getTransactionId(), event.getStatus());
            log.info("Updated Order Status: Txn={} → Status={}",
                    event.getTransactionId(),
                    event.getStatus());

        } catch (Exception e) {
            log.error("Failed to process PaymentResultEvent: {}", e.getMessage());
        }

    }
}
