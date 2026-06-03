package com.library.service;

import com.library.dto.exception.customException.forbiden.ForbiddenUserException;
import com.library.dto.exception.customException.paymentExceptions.PaymentAlreadyExistsException;
import com.library.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.library.dto.exception.customException.paymentExceptions.RefundNotAllowedException;
import com.library.dto.payment.PaymentRequest;
import com.library.dto.payment.PaymentResponse;
import com.library.dto.payment.RefundRequest;
import com.library.entity.Booking;
import com.library.entity.Listing;
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
    class getPayment {
        @Test
        @DisplayName("get payment by booking id should return page of paymentResponse")
        void validRequest_shouldReturnPageOfPaymentResponse() {
            UUID bookingId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            String guestEmail = "guest@gmail.com";
            String hostEmail = "host@gmail.com";
            User user = new User();
            user.setEmail(guestEmail);

            User user2 = new User();
            user2.setEmail(hostEmail);

            Booking booking = new Booking();
            booking.setUser(user);

            Listing listing = new Listing();
            listing.setUser(user2);

            Payment p1 = new Payment();
            Payment p2 = new Payment();

            PaymentResponse pr1 = new PaymentResponse();
            PaymentResponse pr2 = new PaymentResponse();

            List<Payment> prl = List.of(p1, p2);

            Page<Payment> paymentPage = new PageImpl<>(prl);

            when(userService.getCurrentUserEmail()).thenReturn(guestEmail);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBooking_BookingId(bookingId, pageable)).thenReturn(paymentPage);
            when(paymentMapper.toPaymentResponse(p1)).thenReturn(pr1);
            when(paymentMapper.toPaymentResponse(p2)).thenReturn(pr2);

            Page<PaymentResponse> result = paymentService.getPaymentsByBookingId(bookingId, pageable);

            assertThat(result).hasSize(2);
            assertEquals(pr1, result.getContent().get(0));
            assertEquals(pr2, result.getContent().get(1));
        }
        @Test
        @DisplayName("booking not found should throw entitynotfoundexception")
        void bookingNotFound_shouldThrowEntityNotFoundException() {
            UUID bookingId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            String guestEmail = "guest@gmail.com";
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.getPaymentsByBookingId(bookingId, pageable))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Booking not found");
        }

        @Test
        @DisplayName("forbidden user should throw forbiddenuserexxception")
        void forbiddenUser_shouldThrowForbiddenUserException() {
            UUID bookingId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            String guestEmail = "guest@gmail.com";
            String forbiddenEmail = "forbidden@gmail.com";
            String ownerEmail = "owner@gmail.com";


            User guest = new User(); guest.setEmail(guestEmail);
            User host = new User(); host.setEmail(ownerEmail);

            Listing listing = new Listing();
            listing.setUser(host);

            Booking booking = new Booking();
            booking.setUser(new User());
            booking.setUser(guest);
            booking.setListing(listing);

            when(userService.getCurrentUserEmail()).thenReturn(forbiddenEmail);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> paymentService.getPaymentsByBookingId(bookingId, pageable))
                    .isInstanceOf(ForbiddenUserException.class)
                    .hasMessage("It's not your booking. Access denied.");
        }
    }
}
