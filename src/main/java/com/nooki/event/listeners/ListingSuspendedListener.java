package com.nooki.event.listeners;

import com.nooki.event.entities.ListingSuspendedEvent;
import com.nooki.service.BookingServices.BookingCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ListingSuspendedListener {
    private final BookingCommandService bookingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleListingSuspended(ListingSuspendedEvent event) {
        log.info("Started booking cancellation and payment refund for suspended listing: {}", event.listingId());

        try {
            bookingService.cancelAndRefundAllFutureBookings(event.listingId());
        } catch (Exception ignore) {
        }
    }
}
