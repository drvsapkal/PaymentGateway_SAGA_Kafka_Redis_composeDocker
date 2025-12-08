package com.app.paymentsystem.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.paymentsystem.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/simulate/{transactionId}/{orderId}/{amount}")
    public ResponseEntity<?> simulatePayment(
            @PathVariable String transactionId,
            @PathVariable Long orderId,
            @PathVariable Integer amount) {

        boolean result = paymentService.processPayment(transactionId, orderId, amount);

        return ResponseEntity.ok(result ? "PAYMENT SUCCESS" : "PAYMENT FAILED");
    }
    
}