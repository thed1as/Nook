package com.library.service;

import com.library.dto.exception.customException.forbiden.ForbiddenUserException;
import com.library.dto.exception.customException.paymentExceptions.PaymentAlreadyExistsException;
import com.library.dto.exception.customException.paymentExceptions.PaymentNotFoundException;
import com.library.dto.exception.customException.paymentExceptions.RefundNotAllowedException;
import com.library.dto.payment.PaymentRequest;
import com.library.dto.payment.PaymentResponse;
import com.library.dto.payment.RefundRequest;
import com.library.entity.Booking;
import com.library.entity.Payment;
import com.library.entity.User;
import com.library.enums.PaymentStatus;
import com.library.enums.Status;
import com.library.event.entities.BookingCancelEvent;
import com.library.event.entities.BookingConfirmedEvent;
import com.library.event.entities.PaymentCompletedEvent;
import com.library.mapper.PaymentMapper;
import com.library.repository.BookingRepository;
import com.library.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.booking.cancellation-window-days}")
    private int cancellationWindowDays;

    @Transactional
    public String initializePayment(Booking booking, PaymentRequest paymentRequest) {
        String stripeId = stripeService.createPayment(booking.getTotalPrice(), paymentRequest.getCurrency());
        Payment payment = new Payment();
        payment.setUser(booking.getUser());
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setCurrency(paymentRequest.getCurrency().toUpperCase());
        log.debug("paymentMethod: {}", paymentRequest.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setMethod(paymentRequest.getPaymentMethod());
        payment.setStripeId(stripeId);
        paymentRepository.save(payment);

        return stripeId;
    }

    @Transactional
    void processRefundForCancellationBooking(Payment payment) {
        stripeService.refundPayment(payment.getStripeId());

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        eventPublisher.publishEvent(new BookingCancelEvent(
                payment.getBooking().getBookingId(),
                payment.getUser().getEmail(),
                payment.getBooking().getListing().getTitle()
        ));
    }

    @Transactional
    public void handlePaymentSuccess(String stripeId) {
        paymentRepository.findByStripeId(stripeId)
                .ifPresentOrElse(
                        payment -> {
                            if(payment.getStatus() == PaymentStatus.COMPLETED) {
                                log.info("Payment already completed: {}", stripeId);
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
    public void handlePaymentFailure(String stripeId) {
        Payment payment = paymentRepository.findByStripeId(stripeId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if(payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByBookingId(UUID bookingId, Pageable pageable) {

        String userEmail = userService.getCurrentUserEmail();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        if(!booking.getUser().getEmail().equals(userEmail) && !booking.getListing().getUser().getEmail().equals(userEmail)) {
            throw new ForbiddenUserException("It's not your booking. Access denied.");
        }

        return paymentRepository.findByBooking_BookingId(bookingId, pageable)
                .map(paymentMapper::toPaymentResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(Pageable pageable) {
        String email = userService.getCurrentUserEmail();
        return paymentRepository.findByUser_Email(email, pageable)
                .map(paymentMapper::toPaymentResponse);
    }
}
