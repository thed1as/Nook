package com.library.service;

import com.library.dto.payment.PaymentRequest;
import com.library.entity.Booking;
import com.library.entity.Payment;
import com.library.enums.PaymentStatus;
import com.library.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionService {
    private final PaymentRepository paymentRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePendingPayment(Booking booking, PaymentRequest paymentRequest, String stripeId) {
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
    }
}
