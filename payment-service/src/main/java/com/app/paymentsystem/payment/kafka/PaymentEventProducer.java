package com.app.paymentsystem.payment.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.PaymentResultEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class PaymentEventProducer {
//
//    private final KafkaTemplate<String, String> kafkaTemplate;
//    private final ObjectMapper mapper;
//
//    private static final String PAYMENT_RESULT_TOPIC = "payment-result";
//
//    public void sendPaymentSuccess(String transactionId) {
//        publishEvent(transactionId, "SUCCESS");
//    }
//
//    public void sendPaymentFailure(String transactionId) {
//        publishEvent(transactionId, "FAILED");
//    }
//
//    private void publishEvent(String transactionId, String status) {
//        try {
//        	PaymentResultEvent event =
//                    new PaymentResultEvent(transactionId, status);
//
//            String json = mapper.writeValueAsString(event);
//
//            //Key = transactionId (for partition consistency)
//            kafkaTemplate.send(PAYMENT_RESULT_TOPIC, transactionId, json);
//
//            log.info("Published PaymentResultEvent → Topic={} Payload={}", PAYMENT_RESULT_TOPIC, json);
//
//        } catch (Exception e) {
//            log.error("Error publishing PaymentResultEvent: {}", e.getMessage());
//        }
//    }
//}
