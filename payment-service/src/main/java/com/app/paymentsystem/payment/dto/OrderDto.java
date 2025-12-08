package com.app.paymentsystem.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String transactionId;
    private Long customerId;
    private Integer amount;
    private String status;
}
