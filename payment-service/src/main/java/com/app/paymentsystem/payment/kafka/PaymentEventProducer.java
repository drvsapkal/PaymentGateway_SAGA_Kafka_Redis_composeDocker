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

    private static final String SUCCESS_TOPIC = "payment-success";
    private static final String FAILED_TOPIC = "payment-failed";

    public void sendPaymentSuccess(String transactionId, Long orderId) {
        publishEvent(transactionId, orderId, "SUCCESS", SUCCESS_TOPIC);
    }

    public void sendPaymentFailure(String transactionId, Long orderId) {
        publishEvent(transactionId, orderId, "FAILED", FAILED_TOPIC);
    }

    private void publishEvent(String txn, Long orderId, String status, String topic) {
        try {
            PaymentResultEvent event =
                    new PaymentResultEvent(txn, orderId, status);

            String json = mapper.writeValueAsString(event);

            kafkaTemplate.send(topic, txn, json);

            log.info("Published PaymentResultEvent → Topic={} Payload={}", topic, json);

        } catch (Exception e) {
            log.error("Error publishing PaymentResultEvent: {}", e.getMessage());
        }
    }
}
