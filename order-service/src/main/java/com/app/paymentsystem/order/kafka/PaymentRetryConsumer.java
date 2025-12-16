package com.app.paymentsystem.order.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.order.domain.OrderStatus;
import com.app.paymentsystem.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.PaymentResultEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRetryConsumer {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    @KafkaListener(
            topics = "payment-result-retry",
            groupId = "order-service-retry-group",
            errorHandler = "kafkaErrorHandler"
    )
    public void retryPaymentResult(String message) {

        log.warn("Retrying PaymentResultEvent → {}", message);

        try {
            PaymentResultEvent event =
                    objectMapper.readValue(message, PaymentResultEvent.class);

            if ("SUCCESS".equals(event.getStatus())) {
                orderService.updateOrderStatus(
                        event.getTransactionId(),
                        OrderStatus.PAYMENT_SUCCESS
                );
            } else {
                orderService.updateOrderStatus(
                        event.getTransactionId(),
                        OrderStatus.PAYMENT_FAILED
                );
            }

        } catch (Exception e) {
            log.error("Retry failed for PaymentResultEvent", e);
            throw new RuntimeException("Retry PaymentResultEvent failed", e);// 🔥 After retries → DLQ
        }
    }
}
