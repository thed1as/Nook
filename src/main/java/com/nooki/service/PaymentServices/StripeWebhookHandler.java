package com.nooki.service.PaymentServices;

import com.nooki.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.nooki.dto.exception.customException.paymentExceptions.PaymentNotFoundException;
import com.nooki.entity.Booking;
import com.nooki.entity.Payment;
import com.nooki.enums.PaymentStatus;
import com.nooki.enums.Status;
import com.nooki.event.entities.BookingConfirmedEvent;
import com.nooki.event.entities.PaymentCompletedEvent;
import com.nooki.repository.BookingRepository;
import com.nooki.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookHandler {

    @Value("${stripe.api.webhook-secret}")
    private String webhookSecret;

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    protected void handlePaymentSuccess(String stripeId) {
        paymentRepository.findByStripeId(stripeId)
                .ifPresentOrElse(
                        payment -> {
                            if(payment.getStatus() == PaymentStatus.COMPLETED) {
                                log.debug("Payment already completed: {}", stripeId);
                                return;
                            }
                            payment.setStatus(PaymentStatus.COMPLETED);
                            paymentRepository.save(payment);

                            Booking booking = payment.getBooking();
                            booking.setStatus(Status.CONFIRMED);
                            bookingRepository.save(booking);


                            eventPublisher.publishEvent(new PaymentCompletedEvent(
                                    payment.getPaymentId(),
                                    payment.getUser().getEmail(),
                                    booking.getListing().getTitle(),
                                    payment.getAmount(),
                                    payment.getCurrency()
                            ));

                            eventPublisher.publishEvent(new BookingConfirmedEvent(
                                    booking.getBookingId(),
                                    booking.getUser().getEmail(),
                                    booking.getListing().getTitle(),
                                    booking.getCheckInDate(),
                                    booking.getCheckOutDate(),
                                    booking.getTotalPrice()
                            ));
                            log.info("Payment completed: {}", stripeId);
                        },
                        () -> log.warn("Payment not found for stripeId: {}", stripeId)
                );
    }

    @Transactional
    protected void handlePaymentFailure(String stripeId) {
        Payment payment = paymentRepository.findByStripeId(stripeId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if(payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        log.info("Payment marked as FAILED in DB for stripeId: {}", stripeId);
    }

    @Transactional
    public void handleWebHook(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("SECURITY WARNING: Webhook signature verification failed! Potential malicious request");
            throw new PaymentFailedException("BAD REQUEST");
        }

        String eventId = event.getId();
        boolean isNewEvent = markEventAsProcessed(eventId);

        if(!isNewEvent){
            log.info("Duplicate webhook detected and ignored: {}", eventId);
            return;
        }

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    PaymentIntent paymentIntent = (PaymentIntent) event
                            .getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow();

                    handlePaymentSuccess(paymentIntent.getId());
                }

                case "payment_intent.payment_failed" -> {
                    PaymentIntent paymentIntent = (PaymentIntent) event
                            .getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow();

                    handlePaymentFailure(paymentIntent.getId());
                }
                default -> log.debug("Unhandled event: {}", event.getId());
            }
        } catch (Exception e) {
            log.error("Error processing webhook event: {}:",
                    event.getType(), e);
            throw new PaymentFailedException("Payment failed: " + e.getMessage());
        }
    }

    private boolean markEventAsProcessed(String eventId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO processed_webhook_events (event_id, processed_at) VALUES (?, NOW())",
                    eventId
            );
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }
}
