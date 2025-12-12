package com.app.paymentsystem.payment.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.PaymentResultEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String PAYMENT_RESULT_TOPIC = "payment-result";

    public void sendPaymentSuccess(String transactionId, Long orderId) {
        publishEvent(transactionId, orderId, "SUCCESS");
    }

    public void sendPaymentFailure(String transactionId, Long orderId) {
        publishEvent(transactionId, orderId, "FAILED");
    }

    private void publishEvent(String txn, Long orderId, String status) {
        try {
            PaymentResultEvent event =
                    new PaymentResultEvent(txn, orderId, status);

            String json = mapper.writeValueAsString(event);

            kafkaTemplate.send(PAYMENT_RESULT_TOPIC, txn, json);

            log.info("Published PaymentResultEvent → Topic={} Payload={}", PAYMENT_RESULT_TOPIC, json);

        } catch (Exception e) {
            log.error("Error publishing PaymentResultEvent: {}", e.getMessage());
        }
    }
}
