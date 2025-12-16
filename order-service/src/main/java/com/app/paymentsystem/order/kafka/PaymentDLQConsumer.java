package com.app.paymentsystem.order.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentDLQConsumer {

    @KafkaListener(
            topics = "payment-result-dlt",
            groupId = "order-service-dlt-group"
    )
    public void consumeDLQ(String message) {

        log.error(" MESSAGE MOVED TO DLQ  → {}", message);

        // Actions you can do here:
        // 1. Persist to DB
        // 2. Alert via Slack / Email
        // 3. Manual investigation
    }
}
