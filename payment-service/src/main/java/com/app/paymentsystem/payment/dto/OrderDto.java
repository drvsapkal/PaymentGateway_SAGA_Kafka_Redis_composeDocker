package com.app.paymentsystem.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//we sending common-separated strings Before date 09/12/2025 Dto

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
