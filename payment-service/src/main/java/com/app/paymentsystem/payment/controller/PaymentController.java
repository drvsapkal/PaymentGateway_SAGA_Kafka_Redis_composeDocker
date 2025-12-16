package com.app.paymentsystem.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.paymentsystem.payment.service.PaymentService;
import com.saga.events.PaymentRequestedEvent;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    //core responsibility of rest api in event-driven saga just for manual test
    //actual payment must be happen through kafka not Rest
    @PostMapping("/simulate")
    public ResponseEntity<String> simulate(@RequestBody PaymentRequestedEvent event) {
        paymentService.processPayment(event);
        return ResponseEntity.ok("PAYMENT TRIGGERED");
    }
    
}