package com.app.paymentsystem.payment.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

	@Autowired
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String PAYMENT_SUCCESS_TOPIC = "payment-success";
    private static final String PAYMENT_FAILED_TOPIC = "payment-failed";

    public void sendPaymentSuccess(String transactionId, Long orderId) {
        String eventMessage = transactionId + "," + orderId;
        kafkaTemplate.send(PAYMENT_SUCCESS_TOPIC, eventMessage);
        log.info("Published Payment SUCCESS event: {}", eventMessage);
    }

    public void sendPaymentFailure(String transactionId, Long orderId) {
        String eventMessage = transactionId + "," + orderId;
        kafkaTemplate.send(PAYMENT_FAILED_TOPIC, eventMessage);
        log.error("Published Payment FAILURE event: {}", eventMessage);
    }
}
