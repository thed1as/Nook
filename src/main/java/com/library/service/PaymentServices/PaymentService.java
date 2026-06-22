package com.library.service.PaymentServices;

import com.library.dto.exception.customException.bookingException.BookingNotFoundException;
import com.library.dto.exception.customException.forbiden.ForbiddenUserException;
import com.library.dto.payment.PaymentRequest;
import com.library.dto.payment.PaymentResponse;
import com.library.entity.Booking;
import com.library.entity.Payment;
import com.library.enums.PaymentStatus;
import com.library.event.entities.BookingCancelEvent;
import com.library.mapper.PaymentMapper;
import com.library.repository.BookingRepository;
import com.library.repository.PaymentRepository;
import com.library.service.PaymentTransactionService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final StripeService stripeService;
    private final PaymentTransactionService txPayment;
    private final PaymentMapper paymentMapper;
    private final ApplicationEventPublisher eventPublisher;


    public String initializePayment(Booking booking, PaymentRequest paymentRequest) {
        String stripeId = stripeService.createPayment(booking.getTotalPrice(), paymentRequest.getCurrency());

        txPayment.savePendingPayment(booking, paymentRequest, stripeId);
        log.info("Payment entity was created for booking ID: {} with Stripe ID: {}", booking.getBookingId(), stripeId);
        return stripeId;
    }

    @Transactional
    public void processRefundForCancellationBooking(Payment payment) {
        stripeService.refundPayment(payment.getStripeId());

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        eventPublisher.publishEvent(new BookingCancelEvent(
                payment.getBooking().getBookingId(),
                payment.getUser().getEmail(),
                payment.getBooking().getListing().getTitle()
        ));
        log.info("User refunded {} successfully", payment.getPaymentId());
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByBookingId(UUID bookingId, Pageable pageable) {

        UUID userId = userService.getCurrentUserId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        if(!booking.getUser().getUserId().equals(userId) && !booking.getListing().getUser().getUserId().equals(userId)) {
            log.warn("User tried to get access to booking of {} id user", booking.getUser().getUserId());
            throw new ForbiddenUserException("It's not your booking. Access denied.");
        }

        return paymentRepository.findByBooking_BookingId(bookingId, pageable)
                .map(paymentMapper::toPaymentResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(Pageable pageable) {
        UUID userId = userService.getCurrentUserId();
        return paymentRepository.findByUser_UserId(userId, pageable)
                .map(paymentMapper::toPaymentResponse);
    }
}
