package com.app.paymentsystem.payment.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.app.paymentsystem.payment.model.OutboxEvent;
import com.app.paymentsystem.payment.repo.OutboxRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "payment-result";

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OutboxEvent> events =
                outboxRepository.findTop10ByStatusOrderByCreatedAtAsc("NEW");

        for (OutboxEvent event : events) {
            try {
            	ProducerRecord<String, String> record =
            	        new ProducerRecord<>(TOPIC, event.getAggregateId(), event.getPayload());

            	record.headers().add(
            	        "X-Correlation-Id",
            	        event.getCorrelationId().getBytes(StandardCharsets.UTF_8)
            	);

            	kafkaTemplate.send(record);

                event.setStatus("SENT");
                outboxRepository.save(event);
                log.info(
                        "Payment Outbox event SENT → id={} correlationId={}",
                        event.getId(),
                        event.getCorrelationId()
                );

            } catch (Exception ex) {
                log.error("Failed to publish event {}", event.getId(), ex);
            }
        }
    }
}
