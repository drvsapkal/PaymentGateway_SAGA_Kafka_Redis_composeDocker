package com.app.paymentsystem.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import java.nio.charset.StandardCharsets;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultDlqReplayConsumer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	private static final String ORIGINAL_TOPIC = "payment-result";
	private static final String RETRY_HEADER = "dlq-retry-count";
	private static final int MAX_RETRY = 3;

	@KafkaListener(topics = "payment-result.DLT", groupId = "order-service-dlq-replay-group")
	public void replayFromDlq(ConsumerRecord<String, String> record) {

		String key = record.key();
		String payload = record.value();

		// 🔹 Read retry count from header
		Integer retryCount = Optional.ofNullable(record.headers().lastHeader(RETRY_HEADER))
				.map(h -> Integer.parseInt(new String(h.value(), StandardCharsets.UTF_8))).orElse(0);

		if (retryCount >= MAX_RETRY) {
			log.error("❌ DLQ replay exhausted for key={}, payload={}", key, payload);
			return; // parking-lot stop
		}

		log.warn("DLQ replay attempt={} key={} payload={}", retryCount + 1, key, payload);

		// 🔹 Send back to original topic with incremented retry count
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>(ORIGINAL_TOPIC, key, payload);

		producerRecord.headers()
				.add(new RecordHeader(RETRY_HEADER, String.valueOf(retryCount + 1).getBytes(StandardCharsets.UTF_8)));

		kafkaTemplate.send(producerRecord);

	}
}