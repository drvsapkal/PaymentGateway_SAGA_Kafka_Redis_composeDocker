package com.saga.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestedEvent {
    private String transactionId;
    private Long orderId;
    private Integer amount;
}
