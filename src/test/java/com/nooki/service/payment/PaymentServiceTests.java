package com.nooki.service.payment;

import com.nooki.dto.exception.customException.bookingException.BookingNotFoundException;
import com.nooki.dto.exception.customException.forbiden.ForbiddenUserException;
import com.nooki.dto.payment.PaymentResponse;
import com.nooki.entity.Booking;
import com.nooki.entity.Listing;
import com.nooki.entity.Payment;
import com.nooki.entity.User;
import com.nooki.enums.PaymentStatus;
import com.nooki.mapper.PaymentMapper;
import com.nooki.repository.BookingRepository;
import com.nooki.repository.PaymentRepository;
import com.nooki.service.PaymentServices.PaymentService;
import com.nooki.service.PaymentServices.StripeService;
import com.nooki.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PaymentServiceTests {
    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PaymentMapper paymentMapper;

    @Nested
    @DisplayName("get tests")
    class paymentProcessing{

        @Test
        @DisplayName("refund cancellation payment valid request should refund payment")
        void refundCancellationPayment_shouldRefund() {
            Listing l = new Listing();
            l.setTitle("Test");

            Booking booking = new Booking();
            booking.setBookingId(UUID.randomUUID());

            booking.setListing(l);

            User user = new User();
            user.setEmail("useremail@gmail.com");

            Payment payment = new Payment();
            payment.setPaymentId(UUID.randomUUID());
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setStripeId("pi_keytest");

            payment.setBooking(booking);
            payment.setUser(user);

            paymentService.processRefundForCancellationBooking(payment);

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);

            verify(paymentRepository).save(captor.capture());

            assertThat(captor.getValue().getStripeId()).isEqualTo("pi_keytest");
            assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("get payment")
    class getPayment {

        @Test @DisplayName("getPaymentsByBookingId should return page of paymentResponse")
        void getPaymentsByBookingId() {
            Pageable pageable = PageRequest.of(0, 10);
            UUID bookingId = UUID.randomUUID();

            UUID userId = UUID.randomUUID();

            User user = new User();
            user.setUserId(userId);

            Booking booking = new Booking();
            booking.setBookingId(UUID.randomUUID());
            booking.setUser(user);
            booking.setBookingId(bookingId);

            Payment payment1 = new Payment();
            Payment payment2 = new Payment();
            List<Payment> payments = List.of(payment1, payment2);

            Page<Payment> pagePayment = new PageImpl<>(payments, pageable, payments.size());
            PaymentResponse pr1 = new PaymentResponse(); pr1.setPaymentId(UUID.randomUUID());
            PaymentResponse pr2 = new PaymentResponse(); pr2.setPaymentId(UUID.randomUUID());

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(bookingRepository.findById(bookingId))
                    .thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(bookingId, pageable))
                    .thenReturn(pagePayment);
            when(paymentMapper.toPaymentResponse(payment1)).thenReturn(pr1);
            when(paymentMapper.toPaymentResponse(payment2)).thenReturn(pr2);

            Page<PaymentResponse> result = paymentService.getPaymentsByBookingId(bookingId, pageable);

            assertThat(result.getContent().size()).isEqualTo(2);
            assertThat(result.getContent().getFirst().getPaymentId()).isEqualTo(pr1.getPaymentId());
        }

        @Test
        @DisplayName("Booking not found should throw BookingNotFoundException")
        void bookingNotFound_shouldThrowBookingNotFoundException() {
            assertThatThrownBy(() -> paymentService.getPaymentsByBookingId(UUID.randomUUID(), PageRequest.of(0, 10)))
                    .isInstanceOf(BookingNotFoundException.class)
                    .hasMessage("Booking not found");
        }

        @Test
        @DisplayName("Not owner of payment")
        void notOwnerOfPayment_shouldThrowForbiddenUserException() {
            UUID bookingId = UUID.randomUUID();

            UUID hostId = UUID.randomUUID();
            User host = new User();

            host.setUserId(hostId);

            User user = new User();
            user.setUserId(UUID.randomUUID());

            Listing l = new Listing();
            l.setUser(user);

            Booking booking = new Booking();
            booking.setBookingId(bookingId);
            booking.setUser(host);
            booking.setListing(l);

            when(userService.getCurrentUserId()).thenReturn(UUID.randomUUID());
            when(bookingRepository.findById(bookingId))
                    .thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> paymentService.getPaymentsByBookingId(bookingId, PageRequest.of(0, 10)))
                    .isInstanceOf(ForbiddenUserException.class)
                    .hasMessage("It's not your booking. Access denied.");
        }

    }
}
