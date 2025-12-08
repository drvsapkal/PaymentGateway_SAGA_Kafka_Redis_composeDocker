package com.app.paymentsystem.payment.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    @Autowired
	private final PaymentService paymentService;
    
    @Autowired
    private final PaymentEventProducer eventProducer;

    @KafkaListener(topics = "order-created", groupId = "payment-service-group")
    public void consumeOrderCreatedEvent(String message) {
        log.info("Received 'order-created' Kafka event: {}", message);

        String[] parts = message.split(",");
        String transactionId = parts[0];
        Long orderId = Long.valueOf(parts[1]);
        Integer amount = Integer.valueOf(parts[2]);

        boolean paymentStatus = paymentService.processPayment(transactionId, orderId, amount);

        if (paymentStatus) {
            eventProducer.sendPaymentSuccess(transactionId, orderId);
        } else {
            eventProducer.sendPaymentFailure(transactionId, orderId);
        }
    }
}