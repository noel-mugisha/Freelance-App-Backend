package com.demo.backend.controller;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    public record CreatePaymentIntentRequest(BigDecimal amount, String currency) {}

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody CreatePaymentIntentRequest req) throws Exception {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            return ResponseEntity.ok(Map.of("clientSecret", "test_client_secret"));
        }
        Stripe.apiKey = stripeSecretKey;
        long amountInCents = req.amount().multiply(new BigDecimal("100")).longValue();
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(req.currency() == null ? "usd" : req.currency())
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        return ResponseEntity.ok(Map.of("clientSecret", intent.getClientSecret()));
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<?> webhook(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String sig) {
        // For brevity, not verifying signature here. In production, verify using webhook secret.
        return ResponseEntity.ok(Map.of("received", true));
    }
}
