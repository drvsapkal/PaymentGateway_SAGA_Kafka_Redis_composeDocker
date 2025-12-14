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

    //core responsibility of rest api in event-driven saga just for manual test
    //actual payment must be happen through kafka not Rest
    @PostMapping("/simulate/{transactionId}/{orderId}/{amount}")
    public ResponseEntity<?> simulatePayment(
            @PathVariable String transactionId,
            @PathVariable Long orderId,
            @PathVariable Integer amount) {

        paymentService.processPayment(transactionId, orderId, amount);

        return ResponseEntity.ok("Payment processing triggered");
    }
    
}