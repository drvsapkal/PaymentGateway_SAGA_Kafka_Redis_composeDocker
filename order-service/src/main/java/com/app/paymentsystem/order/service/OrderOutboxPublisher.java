package com.app.paymentsystem.order.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.order.entity.OutboxEvent;
import com.app.paymentsystem.order.repo.OutboxRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    private static final String TOPIC = "order-created";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {

        List<OutboxEvent> events =
                outboxRepository.findTop10ByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : events) {
            try {
            	ProducerRecord<String, String> record =
                        new ProducerRecord<>(TOPIC,
                                event.getAggregateId(),
                                event.getPayload());

            	 // correlationId comes from Outbox, NOT MDC
                if (event.getCorrelationId() != null) {
                    record.headers().add(
                            CORRELATION_ID_HEADER,
                            event.getCorrelationId()
                                 .getBytes(StandardCharsets.UTF_8)
                    );
                }
                

                kafkaTemplate.send(record);

                event.setStatus("SENT");
                outboxRepository.save(event);

                log.info(
                        "Order Outbox event SENT → id={} correlationId={}",
                        event.getId(),
                        event.getCorrelationId()
                );

            } catch (Exception e) {
                log.error("Outbox publish failed → id={}", event.getId());
            }
        }
    }
}