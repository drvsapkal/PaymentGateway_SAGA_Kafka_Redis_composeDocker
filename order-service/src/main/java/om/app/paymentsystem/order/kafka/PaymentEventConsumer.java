package om.app.paymentsystem.order.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.app.paymentsystem.order.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

	@Autowired
    private final OrderService orderService;

    // Listen for payment success
    @KafkaListener(topics = "payment-success", groupId = "order-service-group")
    public void consumePaymentSuccess(String message) {
        log.info("Received payment-success event: {}", message);

        String[] parts = message.split(",");
        String transactionId = parts[0];
        Long orderId = Long.valueOf(parts[1]);

        orderService.updateOrderStatus(transactionId, "PAID");
        log.info("Order status updated to PAID for OrderId={} TxnId={}", orderId, transactionId);
    }

    // Listen for payment failure
    @KafkaListener(topics = "payment-failed", groupId = "order-service-group")
    public void consumePaymentFailure(String message) {
        log.info("Received payment-failed event: {}", message);

        String[] parts = message.split(",");
        String transactionId = parts[0];
        Long orderId = Long.valueOf(parts[1]);

        orderService.updateOrderStatus(transactionId, "PAYMENT_FAILED");
        log.info("Order status updated to PAYMENT_FAILED for OrderId={} TxnId={}", orderId, transactionId);
    }
}
