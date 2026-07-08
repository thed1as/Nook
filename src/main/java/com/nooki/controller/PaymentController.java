package com.nooki.controller;

import com.nooki.dto.payment.PaymentResponse;
import com.nooki.service.PaymentServices.PaymentService;
import com.nooki.service.PaymentServices.StripeWebhookHandler;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Payment", description = "Payment API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    @Value("${stripe.api.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;
    private final StripeWebhookHandler webhookHandler;

    @Hidden
    @PostMapping("/stripe-notifications")
    public ResponseEntity<String> handleWebHook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        webhookHandler.handleWebHook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/payments/booking/{bookingId}")
    public ResponseEntity<Page<PaymentResponse>> getPayments(@PathVariable("bookingId") UUID bookingId,
                                                             @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentResponse> paymentResponses = paymentService.getPaymentsByBookingId(bookingId, pageable);
        return ResponseEntity.ok(paymentResponses);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/payments/my")
    public ResponseEntity<Page<PaymentResponse>> getMyPayments(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentResponse> paymentResponses = paymentService.getUserPayments(pageable);
        return ResponseEntity.ok(paymentResponses);
    }
}
