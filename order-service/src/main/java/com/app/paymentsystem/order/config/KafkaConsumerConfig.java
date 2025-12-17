package com.app.paymentsystem.order.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;


@Configuration
@EnableKafka
public class KafkaConsumerConfig {

	@Bean
	public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, String> kafkaTemplate,
			MeterRegistry meterRegistry) {

		Counter dlqCounter = meterRegistry.counter("kafka.consumer.dlq.count");

		return new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) -> {
			dlqCounter.increment(); // ✅ DLQ metric
			return new TopicPartition(record.topic() + "-dlt", record.partition());
		});
	}

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer, MeterRegistry meterRegistry) {

		FixedBackOff backOff = new FixedBackOff(5000L, 3);

		DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

		Counter retryCounter = meterRegistry.counter("kafka.consumer.retry.count");

		handler.setRetryListeners((record, ex, deliveryAttempt) -> {
			retryCounter.increment(); // ✅ Retry metric
		});

		handler.addNotRetryableExceptions(IllegalArgumentException.class);

		return handler;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
			ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler kafkaErrorHandler) {

		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(kafkaErrorHandler);

		return factory;
	}
}
/*
 * //| Feature 				| Status | | 
 * | | Retry (3 times) 		| ✅
 * | | DLQ routing 			| ✅ 
 * | | Retry metrics 		| ✅ 
 * | | DLQ metrics 			| ✅ 
 * | | Compile * error 		| ❌ fixed 
 * | | No extra consumers 	| ✅ 
 * | | Industry standard 	| ✅ |
 */

