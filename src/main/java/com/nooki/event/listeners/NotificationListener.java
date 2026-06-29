package com.nooki.event.listeners;

import com.nooki.event.entities.BookingCancelEvent;
import com.nooki.event.entities.BookingConfirmedEvent;
import com.nooki.event.entities.PaymentCompletedEvent;
import com.nooki.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {
    private final EmailService emailService;

    @EventListener
    @Async
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Sending booking confirmation for: {}", event.getBookingId());
        emailService.sendBookingConfirmation(
                event.getUserEmail(),
                event.getListingTitle(),
                event.getCheckIn(),
                event.getCheckOut(),
                event.getTotalAmount()
        );
    }

    @EventListener
    @Async
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Sending payment completed for: {}", event.getPaymentId());
        emailService.sendPaymentConfirmation(
                event.getUserEmail(),
                event.getListingTitle(),
                event.getAmount(),
                event.getCurrency()
        );
    }

    @EventListener
    @Async
    public void handleBookingCanceled(BookingCancelEvent event) {
        log.info("Sending booking cancel for: {}", event.getBookingId());
        emailService.sendBookingCancellation(
                event.getUserEmail(),
                event.getListingTitle()
        );
    }
}
