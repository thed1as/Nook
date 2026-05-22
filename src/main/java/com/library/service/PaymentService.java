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
import com.library.mapper.PaymentMapper;
import com.library.repository.BookingRepository;
import com.library.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.booking.cancellation-window-days}")
    private int cancellationWindowDays;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        User user = userService.getUserByEmail(userService.getCurrentUserEmail());
        Booking booking = bookingRepository.findById(paymentRequest.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        if(!booking.getUser().equals(user)) {
            throw new IllegalStateException("Not your bookings!");
        }

        if(!booking.getStatus().equals(Status.PENDING)) {
            throw new IllegalStateException("Booking status is not PENDING!");
        }

        if(paymentRepository.existsPaymentByBooking_BookingIdAndStatus(
                booking.getBookingId(), PaymentStatus.COMPLETED
        )) {
            throw new PaymentAlreadyExistsException("Payment already exists");
        }

        Payment payment = new Payment();

        String stripeId = stripeService.createPayment(
                booking.getTotalPrice(),
                paymentRequest.getCurrency()
        );

        payment.setUser(user);
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        log.debug("paymentMethod: {}", paymentRequest.getPaymentMethod());
        payment.setMethod(paymentRequest.getPaymentMethod());
        payment.setStripeId(stripeId);

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse refundPayment(RefundRequest refundRequest) {
        User user = userService.getUserByEmail(userService.getCurrentUserEmail());
        Payment payment = paymentRepository.findById(refundRequest.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        Booking booking = payment.getBooking();

        if(!payment.getUser().equals(user)) {
            throw new ForbiddenUserException("Not your payment or booking");
        }

        if(!payment.getStatus().equals(PaymentStatus.COMPLETED)) {
            throw new RefundNotAllowedException("Your payment isn't completed");
        }


        if(!booking.getStatus().equals(Status.CONFIRMED)) {
            throw new RefundNotAllowedException("Booking cancelled or was completed");
        }

        if((LocalDateTime.now().isAfter(booking.getCheckInDate().minusDays(cancellationWindowDays)) ||
                LocalDateTime.now().isAfter(booking.getCreatedAt().plusDays(1)))) {
            throw new RefundNotAllowedException("Too late to refund payment and cancel booking");
        }

        stripeService.refundPayment(payment.getStripeId());
        payment.setStatus(PaymentStatus.REFUNDED);
        booking.setStatus(Status.CANCELLED);

        bookingRepository.save(booking);
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
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

        UUID userId = userService.getUserByEmail(userService.getCurrentUserEmail()).getUserId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        if(!booking.getUser().getUserId().equals(userId) && !booking.getListing().getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("It's not your booking. Access denied.");
        }

        return paymentRepository.findByBooking_BookingId(bookingId, pageable)
                .map(paymentMapper::toPaymentResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(Pageable pageable) {
        UUID userId = userService.getUserByEmail(userService.getCurrentUserEmail()).getUserId();
        return paymentRepository.findByUser_UserId(userId, pageable)
                .map(paymentMapper::toPaymentResponse);
    }
}
