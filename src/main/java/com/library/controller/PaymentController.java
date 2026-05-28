package com.library.controller;

import com.library.dto.payment.PaymentResponse;
import com.library.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Payment", description = "Payment API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    @Value("${stripe.api.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;

    @Hidden
    @PostMapping("/stripe-notifications")
    public ResponseEntity<String> handleWebHook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().build();
        }

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    PaymentIntent paymentIntent = (PaymentIntent) event
                            .getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow();

                    paymentService.handlePaymentSuccess(paymentIntent.getId());
                    log.info("Payment successful {}", paymentIntent.getId());
                }

                case "payment_intent.payment_failed" -> {
                    PaymentIntent paymentIntent = (PaymentIntent) event
                            .getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow();
                    paymentService.handlePaymentFailure(paymentIntent.getId());
                    log.info("Payment failed {}", paymentIntent.getId());
                }
                default -> log.info("Unhandled event: {}", event.getId());
            }
        } catch (Exception e) {
            log.error("Error processing webhook event: {}: {}",
                    event.getType(), e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @GetMapping("/payments/booking/{bookingId}")
    public ResponseEntity<Page<PaymentResponse>> getPayments(@PathVariable("bookingId") UUID bookingId, Pageable pageable) {
        Page<PaymentResponse> paymentResponses = paymentService.getPaymentsByBookingId(bookingId, pageable);
        return ResponseEntity.ok(paymentResponses);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/payments/my")
    public ResponseEntity<Page<PaymentResponse>> getMyPayments(Pageable pageable) {
        Page<PaymentResponse> paymentResponses = paymentService.getUserPayments(pageable);
        return ResponseEntity.ok(paymentResponses);
    }
}
