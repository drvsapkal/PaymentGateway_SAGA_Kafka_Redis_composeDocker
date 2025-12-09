package com.saga.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultEvent {
    private String transactionId;
    private Long orderId;
    private String status;  // SUCCESS or FAILED
}
