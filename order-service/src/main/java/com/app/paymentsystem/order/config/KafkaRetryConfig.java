package com.app.paymentsystem.order.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;


//📌 This is exactly how Spring Kafka is used in production.
@Configuration
@EnableKafka
public class KafkaRetryConfig {

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, String> kafkaTemplate) {

        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    // Route to retry first, then DLT
                    if (record.topic().equals("payment-result")) {
                        return new TopicPartition("payment-result-retry", record.partition());
                    }
                    return new TopicPartition("payment-result-dlt", record.partition());
                }
        );
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer recoverer) {

        // Retry 3 times with 5s delay
        FixedBackOff backOff = new FixedBackOff(5000L, 3);

        DefaultErrorHandler handler =
                new DefaultErrorHandler(recoverer, backOff);

        // Do NOT retry these (business-safe)
        handler.addNotRetryableExceptions(
                IllegalArgumentException.class
        );

        return handler;
    }
}