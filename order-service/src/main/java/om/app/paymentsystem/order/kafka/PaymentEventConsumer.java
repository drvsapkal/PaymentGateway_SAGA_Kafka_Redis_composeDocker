package om.app.paymentsystem.order.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.events.PaymentResultEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

	@Autowired
    private final OrderService orderService;
	
	private final ObjectMapper mapper = new ObjectMapper();

    // Listen for payment success
    @KafkaListener(topics = "payment-success", groupId = "order-service-group")
    public void consumePaymentResponse(String message) {

    	try {
            PaymentResultEvent event = mapper.readValue(message, PaymentResultEvent.class);

            log.info("Received PaymentResult event: {}", event);

            orderService.updateOrderStatus(event.getTransactionId(), event.getStatus());
            log.info("Order status updated to {}", event.getStatus());

        } catch (Exception e) {
            log.error("Failed to process PaymentResultEvent: {}", e.getMessage());
        }

    }
}
