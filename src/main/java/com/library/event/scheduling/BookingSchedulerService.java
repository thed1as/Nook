package com.library.event.scheduling;

import com.library.enums.PaymentStatus;
import com.library.enums.Status;
import com.library.repository.BookingRepository;
import com.library.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class BookingSchedulerService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        log.info("Looking for expired bookings created before: {}", threshold);

        int updatedPaymentCount = paymentRepository.cancelExpiredPayments(PaymentStatus.CANCELLED, PaymentStatus.PENDING, Status.CANCELLED, threshold);
        if(updatedPaymentCount > 0) {
            log.info("Successfully cancelled expired {} pending payments", updatedPaymentCount);
        }
        int updatedBookingCount = bookingRepository.cancelExpiredBookings(Status.CANCELLED, Status.PENDING, threshold);
        if(updatedBookingCount == updatedPaymentCount ) {
            log.info("Successfully cancelled expired {} pending books", updatedBookingCount);
        }
    }
}
