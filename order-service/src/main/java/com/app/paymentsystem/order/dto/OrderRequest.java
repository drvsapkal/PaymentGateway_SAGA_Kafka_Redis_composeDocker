package com.app.paymentsystem.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//we sending common-separated strings Before date 09/12/2025 Dto

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private Long customerId;
    private Integer amount;
    private Long productId;
    private int quantity;

}