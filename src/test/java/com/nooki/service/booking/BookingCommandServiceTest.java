package com.nooki.service.booking;

import com.nooki.dto.booking.BookingRequest;
import com.nooki.dto.booking.BookingResponse;
import com.nooki.dto.checkout.BookingCheckoutRequest;
import com.nooki.dto.checkout.BookingCheckoutResponse;
import com.nooki.dto.exception.customException.bookingException.BookingAccessDeniedException;
import com.nooki.dto.exception.customException.bookingException.BookingCancelException;
import com.nooki.dto.exception.customException.bookingException.BookingIllegalStateException;
import com.nooki.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.nooki.dto.payment.PaymentRequest;
import com.nooki.entity.Booking;
import com.nooki.entity.Listing;
import com.nooki.entity.Payment;
import com.nooki.entity.User;
import com.nooki.enums.PaymentMethod;
import com.nooki.enums.PaymentStatus;
import com.nooki.enums.Status;
import com.nooki.mapper.BookingCheckoutMapper;
import com.nooki.mapper.BookingMapper;
import com.nooki.repository.BookingRepository;
import com.nooki.service.BookingServices.BookingCommandService;
import com.nooki.service.BookingServices.BookingTransactionService;
import com.nooki.service.PaymentServices.PaymentService;
import com.nooki.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingCommandServiceTest {
    @InjectMocks
    private BookingCommandService bookingCommandService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private BookingCheckoutMapper bookingCheckoutMapper;

    @Mock
    private BookingTransactionService bookingTransactionService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Nested
    @DisplayName("Creating booking")
    class createBooking {
        private final UUID listingId = UUID.randomUUID();
        private final LocalDateTime checkInDate = LocalDateTime.now().plusDays(1)
                .with(LocalTime.NOON);
        private final LocalDateTime checkOutDate = LocalDateTime.now().plusDays(3)
                .with(LocalTime.NOON);
        private final String toCurrency = "EUR";
        private final PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        private final String stripeId = "pi_testtesttesttesttesttest";

        private BookingCheckoutRequest createRequest() {
            BookingRequest br = new BookingRequest();
            br.setListingId(listingId);
            br.setCheckInDate(checkInDate);
            br.setCheckOutDate(checkOutDate);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentMethod(paymentMethod);
            paymentRequest.setCurrency(toCurrency);

            BookingCheckoutRequest bcr = new BookingCheckoutRequest();
            bcr.setPaymentRequest(paymentRequest);
            bcr.setBookingRequest(br);
            return bcr;
        }

        private Booking createEntity() {
            Booking b = new Booking();
            b.setCurrency(toCurrency);
            b.setStatus(Status.PENDING);
            b.setCheckInDate(checkInDate);
            b.setCheckOutDate(checkOutDate);
            return b;
        }

        @Test
        @DisplayName("Valid should return BookingCheckoutResponse")
        void validRequest_shouldReturnBookingCheckoutResponse() {
             BookingCheckoutRequest req = createRequest();
             Booking booking = createEntity();

            BookingResponse bookingResponse = new BookingResponse();
            bookingResponse.setCheckInDate(checkInDate);
            bookingResponse.setCheckOutDate(checkOutDate);
            bookingResponse.setCurrency(toCurrency);
            bookingResponse.setStatus(Status.PENDING);

            BookingCheckoutResponse expectedResult = new BookingCheckoutResponse();
            expectedResult.setBookingResponse(bookingResponse);
            expectedResult.setStripeId(stripeId);

            when(bookingTransactionService.reserveBooking(req.getBookingRequest(), req.getPaymentRequest().getCurrency()))
                     .thenReturn(booking);
            when(paymentService.initializePayment(booking, req.getPaymentRequest()))
                     .thenReturn(stripeId);
            when(bookingMapper.toBookingResponse(booking)).thenReturn(bookingResponse);
            when(bookingCheckoutMapper.toBookingCheckoutResponse(any(BookingResponse.class), eq(stripeId)))
                    .thenReturn(expectedResult);


            BookingCheckoutResponse result = bookingCommandService.create(req);

            assertThat(result).isNotNull();
            assertThat(result.getBookingResponse()).isEqualTo(bookingResponse);
            assertThat(result.getStripeId()).isEqualTo(stripeId);
        }

        @Test
        @DisplayName("Wrong request should throw BookingIllegalStateException")
        void checkOutBeforeCheckIn_shouldThrowBookingIllegalStateException() {
            BookingCheckoutRequest req = createRequest();
            BookingRequest wrongRequest = new BookingRequest();
            wrongRequest.setCheckInDate(checkOutDate);
            wrongRequest.setCheckOutDate(checkInDate);
            req.setBookingRequest(wrongRequest);

            assertThatThrownBy(() -> bookingCommandService.create(req))
                    .isInstanceOf(BookingIllegalStateException.class)
                    .hasMessage("Check-out have to be after check-in");
        }

        @Test
        @DisplayName("Failed to initialize payment should throw PaymentFailedException")
        void paymentInitFailed_shouldThrowPaymentFailedException() {
            BookingCheckoutRequest req = createRequest();
            Booking booking = createEntity();

            when(bookingTransactionService.reserveBooking(req.getBookingRequest(), req.getPaymentRequest().getCurrency()))
                    .thenReturn(booking);

            when(paymentService.initializePayment(booking, req.getPaymentRequest()))
                    .thenThrow(RuntimeException.class);

            assertThatThrownBy(() -> bookingCommandService.create(req))
                    .isInstanceOf(PaymentFailedException.class);
        }

    }

    @Nested
    @DisplayName("Cancel booking")
    class cancelBooking {
        private final UUID userId = UUID.randomUUID();
        private final UUID bookingId = UUID.randomUUID();

        @Test
        @DisplayName("successfully cancelled booking should return response")
        void cancelledBooking_shouldReturnResponse() {
            User user = new User();
            user.setUserId(userId);

            Booking booking = new Booking();
            booking.setStatus(Status.CONFIRMED);

            Payment payment = new Payment();
            payment.setStatus(PaymentStatus.COMPLETED);

            booking.setUser(user);
            booking.setListing(new Listing());
            booking.setPayment(payment);

            BookingResponse expected = new BookingResponse();
            expected.setStatus(Status.CANCELLED);

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(bookingRepository.findDetailedForCancelById(bookingId))
                    .thenReturn(Optional.of(booking));
            when(bookingMapper.toBookingResponse(booking))
                    .thenReturn(expected);

            BookingResponse result = bookingCommandService.cancel(bookingId);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expected);
            assertThat(booking.getStatus()).isEqualTo(Status.CANCELLED);

            verify(bookingRepository, times(1)).save(booking);
        }

        @Test
        @DisplayName("Already cancelled status should throw BookingCancelException")
        void cancelledBooking_shouldThrowBookingCancelException() {
            Booking booking = new Booking();
            booking.setStatus(Status.CANCELLED);

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(bookingRepository.findDetailedForCancelById(bookingId))
                    .thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingCommandService.cancel(bookingId))
                    .isInstanceOf(BookingCancelException.class)
                    .hasMessage("Cannot cancel booking with status: " + booking.getStatus());
        }

        @Test
        @DisplayName("not owner of booking should throw BookingAccessDeniedException")
        void notOwnerOfBooking_shouldThrowBookingAccessDeniedException() {
            User host = new User();
            host.setUserId(UUID.randomUUID());
            Listing listing = new Listing();
            listing.setUser(host);

            Booking booking = new Booking();
            booking.setStatus(Status.PENDING);

            User user = new User();
            user.setUserId(UUID.randomUUID());

            booking.setUser(user);
            booking.setListing(listing);

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(bookingRepository.findDetailedForCancelById(bookingId))
                    .thenReturn(Optional.of(booking));
            assertThatThrownBy(() -> bookingCommandService.cancel(bookingId))
                    .isInstanceOf(BookingAccessDeniedException.class)
                    .hasMessage("You isn't owner of booking");
        }
    }
}
