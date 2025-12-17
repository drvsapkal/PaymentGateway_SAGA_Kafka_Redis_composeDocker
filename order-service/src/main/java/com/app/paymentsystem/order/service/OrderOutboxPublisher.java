package com.app.paymentsystem.order.service;

import java.util.List;

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

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {

        List<OutboxEvent> events =
                outboxRepository.findTop10ByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                        "order-created",
                        event.getAggregateId(),
                        event.getPayload()
                );

                event.setStatus("SENT");
                outboxRepository.save(event);

                log.info("Outbox event SENT → id={}", event.getId());

            } catch (Exception e) {
                log.error("Outbox publish failed → id={}", event.getId());
            }
        }
    }
}

