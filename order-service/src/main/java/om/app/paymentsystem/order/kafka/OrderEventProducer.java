package om.app.paymentsystem.order.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.saga.events.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String ORDER_CREATED_TOPIC = "order-created";

    @Autowired
    private final KafkaTemplate<String, String> kafkaTemplate = null;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
    	
    	try {
            String message = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(event);
            kafkaTemplate.send(ORDER_CREATED_TOPIC, event.getTransactionId(), message);
            log.info("Published OrderCreatedEvent → Topic='{}' Payload={}", ORDER_CREATED_TOPIC, message);
        } catch (Exception e) {
            log.error("Error publishing OrderCreatedEvent: {}", e.getMessage());
        }
    }

    
}
