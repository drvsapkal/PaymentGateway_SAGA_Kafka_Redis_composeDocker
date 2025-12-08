package com.app.paymentsystem.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

	private String transactionId;
    private Long customerId;
    private Integer amount;

}