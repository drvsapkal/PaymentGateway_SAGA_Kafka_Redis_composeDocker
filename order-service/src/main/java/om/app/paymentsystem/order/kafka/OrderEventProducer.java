package om.app.paymentsystem.order.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.order.entity.Order;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "order-created";

    @Autowired
    private final KafkaTemplate<String, String> kafkaTemplate = null;

    public void sendOrderCreatedEvent(Order order) {
        String payload = order.getTransactionId() + "," + order.getId() + "," + order.getAmount();
        log.info("Sending order-created event: {}", payload);
        kafkaTemplate.send(TOPIC, payload);
    }
    
}
