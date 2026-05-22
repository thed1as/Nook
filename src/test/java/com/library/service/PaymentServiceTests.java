package com.library.service;

import com.library.dto.exception.customException.paymentExceptions.PaymentAlreadyExistsException;
import com.library.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.library.dto.exception.customException.paymentExceptions.RefundNotAllowedException;
import com.library.dto.payment.PaymentRequest;
import com.library.dto.payment.PaymentResponse;
import com.library.dto.payment.RefundRequest;
import com.library.entity.Booking;
import com.library.entity.Payment;
import com.library.entity.User;
import com.library.enums.PaymentMethod;
import com.library.enums.PaymentStatus;
import com.library.enums.Status;
import com.library.mapper.PaymentMapper;
import com.library.repository.BookingRepository;
import com.library.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private PaymentMapper paymentMapper;

    @Nested
    @DisplayName("Create payment")
    class CreatePayment {
        @Test
        @DisplayName("Valid request successful payment")
        void validRequest_ReturnsPaymentResponse() {
            String mockStripePaymentIntentId = "pi_test_" + UUID.randomUUID().toString().substring(0, 8);
            PaymentRequest paymentRequest = new PaymentRequest();
            UUID bookingId = UUID.randomUUID();
            String currency = "USD";

            paymentRequest.setBookingId(bookingId);
            paymentRequest.setCurrency(currency);
            paymentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);

            User user = new User();
            String testEmail = "test@gmail.com";
            user.setEmail(testEmail);

            Booking booking = new Booking();
            booking.setBookingId(bookingId);
            booking.setStatus(Status.PENDING);
            booking.setUser(user);
            booking.setTotalPrice(BigDecimal.valueOf(500));

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setPaymentId(UUID.randomUUID());
            paymentResponse.setCurrency(currency);
            paymentResponse.setAmount(booking.getTotalPrice());
            paymentResponse.setStatus(PaymentStatus.PENDING);
            paymentResponse.setCreatedAt(LocalDateTime.now());
            paymentResponse.setBookingId(bookingId);

            when(userService.getCurrentUserEmail()).thenReturn(testEmail);
            when(userService.getUserByEmail(testEmail))
                    .thenReturn(user);

            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            when(paymentRepository.existsPaymentByBooking_BookingIdAndStatus(
                    booking.getBookingId(), PaymentStatus.COMPLETED
            )).thenReturn(false);

            when(stripeService.createPayment(booking.getTotalPrice(), paymentRequest.getCurrency()))
                    .thenReturn(mockStripePaymentIntentId);

            Payment mockPayment = new Payment();

            when(paymentRepository.save(any(Payment.class)))
                    .thenReturn(mockPayment);

            when(paymentMapper.toPaymentResponse(any(Payment.class)))
                    .thenReturn(paymentResponse);

            PaymentResponse result = paymentService.createPayment(paymentRequest);

            assertThat(result).isNotNull();
            assertThat(result.getBookingId()).isEqualTo(bookingId);

            verify(stripeService, times(1)).createPayment(booking.getTotalPrice(), paymentRequest.getCurrency());
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @Test
        @DisplayName("Booking not found")
        void bookingNotFound_ThrowsEntityNotFoundException() {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setBookingId(UUID.randomUUID());
            paymentRequest.setCurrency("USD");
            paymentRequest.setPaymentMethod(PaymentMethod.DEBIT_CARD);


            when(userService.getCurrentUserEmail()).thenReturn("test@gmail.com");
            when(userService.getUserByEmail("test@gmail.com")).thenReturn(new User());
            when(bookingRepository.findById(paymentRequest.getBookingId())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Booking not found");

            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("It's not your booking should throw IllegalStateException")
        void notYourBooking_ThrowsIllegalStateException() {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setBookingId(UUID.randomUUID());
            paymentRequest.setPaymentMethod(PaymentMethod.PAYPAL);
            paymentRequest.setCurrency("USD");

            String testEmail = "test@gmail.com";
            User user = new User();
            user.setEmail(testEmail);

            User user2 = new User();

            Booking booking = new Booking();
            booking.setUser(user2);

            when(userService.getCurrentUserEmail()).thenReturn(testEmail);
            when(userService.getUserByEmail(testEmail)).thenReturn(user);
            when(bookingRepository.findById(paymentRequest.getBookingId()))
                    .thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Not your bookings!");

            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Payment already exists")
        void paymentExists_shouldThrowPaymentAlreadyExists() {
            UUID bookingId = UUID.randomUUID();
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setBookingId(bookingId);
            paymentRequest.setPaymentMethod(PaymentMethod.PAYPAL);
            paymentRequest.setCurrency("USD");

            String testEmail = "test@gmail.com";
            User user = new User();
            user.setEmail(testEmail);

            Booking booking = new Booking();
            booking.setBookingId(bookingId);
            booking.setStatus(Status.PENDING);
            booking.setUser(user);

            when(userService.getCurrentUserEmail()).thenReturn(testEmail);
            when(userService.getUserByEmail(testEmail)).thenReturn(user);
            when(bookingRepository.findById(paymentRequest.getBookingId()))
                    .thenReturn(Optional.of(booking));

            when(paymentRepository.existsPaymentByBooking_BookingIdAndStatus
                    (booking.getBookingId(), PaymentStatus.COMPLETED))
                    .thenReturn(true);

            assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                    .isInstanceOf(PaymentAlreadyExistsException.class)
                    .hasMessage("Payment already exists");

            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Stripe service failed")
        void stripeFailed_shouldThrowPaymentFailedException() {
            PaymentRequest paymentRequest = new PaymentRequest();
            UUID bookingId = UUID.randomUUID();
            paymentRequest.setBookingId(bookingId);
            paymentRequest.setPaymentMethod(PaymentMethod.PAYPAL);
            paymentRequest.setCurrency("USD");

            String testEmail = "test@gmail.com";
            User user = new User();
            user.setEmail(testEmail);

            Booking booking = new Booking();
            booking.setBookingId(bookingId);
            booking.setUser(user);
            booking.setStatus(Status.PENDING);
            booking.setTotalPrice(BigDecimal.valueOf(500));

            when(userService.getCurrentUserEmail()).thenReturn(testEmail);
            when(userService.getUserByEmail(testEmail)).thenReturn(user);
            when(bookingRepository.findById(paymentRequest.getBookingId()))
                    .thenReturn(Optional.of(booking));

            when(paymentRepository.existsPaymentByBooking_BookingIdAndStatus(
                    booking.getBookingId(), PaymentStatus.COMPLETED
            )).thenReturn(false);
            when(stripeService.createPayment(booking.getTotalPrice(), paymentRequest.getCurrency()))
                    .thenThrow(PaymentFailedException.class);

            assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                    .isInstanceOf(PaymentFailedException.class);

            verify(paymentRepository, never()).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("Refund payment")
    class RefundPayment {
        @Test
        @DisplayName("valid request should Refund the payment successfully and return paymentResponse")
        void successful_shouldReturnPaymentResponse() {
            UUID paymentId = UUID.randomUUID();
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setPaymentId(paymentId);
            refundRequest.setReason("Refund testing");

            String testEmail = "test@gmail.com";
            User user = new User();
            user.setEmail(testEmail);

            Booking booking = new Booking();
            booking.setStatus(Status.CONFIRMED);
            booking.setCheckInDate(LocalDateTime.now().plusDays(4));
            booking.setCreatedAt(LocalDateTime.now().plusHours(10));

            Payment payment = new Payment();
            payment.setStatus(PaymentStatus.COMPLETED);


            payment.setUser(user);
            booking.setUser(user);
            payment.setBooking(booking);

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setPaymentId(paymentId);
            paymentResponse.setStatus(PaymentStatus.REFUNDED);

            when(userService.getCurrentUserEmail()).thenReturn(testEmail);
            when(userService.getUserByEmail(testEmail)).thenReturn(user);
            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.of(payment));

            when(paymentRepository.save(payment)).thenReturn(payment);
            when(paymentMapper.toPaymentResponse(any(Payment.class)))
                    .thenReturn(paymentResponse);

            PaymentResponse result = paymentService.refundPayment(refundRequest);

            assertThat(result).isNotNull();
            assertThat(booking.getStatus()).isEqualTo(Status.CANCELLED);
            assertThat(result.getPaymentId()).isEqualTo(paymentId);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

            verify(bookingRepository, times(1)).save(any(Booking.class));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @Test
        @DisplayName("Payment not completed should throw new RefundNotAllowedException")
        void paymentNotCompleted_shouldThrowRefundNotAllowedException() {
            UUID paymentId = UUID.randomUUID();

            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setPaymentId(paymentId);
            refundRequest.setReason("Refund testing");

            String testEmail = "test@gmail.com";
            User user = new User();
            user.setEmail(testEmail);

            Payment payment = new Payment();
            payment.setPaymentId(paymentId);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUser(user);

            when(userService.getCurrentUserEmail())
                    .thenReturn(testEmail);
            when(userService.getUserByEmail(testEmail)).thenReturn(user);
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.refundPayment(refundRequest))
                    .isInstanceOf(RefundNotAllowedException.class)
                    .hasMessage("Your payment isn't completed");

            verify(paymentRepository, never()).save(any(Payment.class));
            verify(bookingRepository, never()).save(any(Booking.class));
        }
    }
}
