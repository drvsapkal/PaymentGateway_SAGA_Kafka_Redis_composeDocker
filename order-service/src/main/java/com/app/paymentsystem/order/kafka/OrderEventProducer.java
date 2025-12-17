package com.app.paymentsystem.order.kafka;

//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OrderEventProducer {
//
//    private static final String ORDER_CREATED_TOPIC = "order-created";
//
//    private final KafkaTemplate<String, String> kafkaTemplate;    
//
//    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
//    	
//    	try {
//    		//insteaded of creating Object mapper at separate line
//            String message = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(event);
//            kafkaTemplate.send(ORDER_CREATED_TOPIC, event.getTransactionId(), message);
//            log.info("Published OrderCreatedEvent → Topic='{}' Payload={}", ORDER_CREATED_TOPIC, message);
//        } catch (Exception e) {
//            log.error("Error publishing OrderCreatedEvent: {}", e.getMessage());
//        }
//    }
//
//    
//}
