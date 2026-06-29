package com.nooki.service.payment;

import com.nooki.dto.payment.PaymentRequest;
import com.nooki.entity.Booking;
import com.nooki.entity.Payment;
import com.nooki.entity.User;
import com.nooki.enums.PaymentMethod;
import com.nooki.enums.PaymentStatus;
import com.nooki.repository.PaymentRepository;
import com.nooki.service.PaymentServices.PaymentTransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PaymentTransactionServiceTest {
    @InjectMocks
    private PaymentTransactionService paymentTransactionService;

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("creating and saving pending payment")
    void createAndSavePayment() {
        User user = new User();

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTotalPrice(new BigDecimal("300.00"));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(PaymentMethod.DEBIT_CARD);
        paymentRequest.setCurrency("usd");

        String stripeId = "pi_..." + UUID.randomUUID().toString();

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);

        paymentTransactionService.savePendingPayment(booking, paymentRequest, stripeId);

        verify(paymentRepository, times(1)).save(captor.capture());

        Payment p = captor.getValue();

        assertThat(p.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(p.getBooking()).isEqualTo(booking);
        assertThat(p.getUser()).isEqualTo(booking.getUser());
        assertThat(p.getAmount()).isEqualTo(booking.getTotalPrice());
    }
}
