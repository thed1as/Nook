package com.library.service;

import com.library.dto.booking.BookingRequest;
import com.library.dto.checkout.BookingCheckoutRequest;
import com.library.dto.checkout.BookingCheckoutResponse;
import com.library.dto.exception.customException.forbiden.ForbiddenUserException;
import com.library.dto.payment.PaymentRequest;
import com.library.entity.Booking;
import com.library.entity.Listing;
import com.library.entity.Payment;
import com.library.entity.User;
import com.library.enums.PaymentMethod;
import com.library.enums.PaymentStatus;
import com.library.enums.Status;
import com.library.mapper.BookingCheckoutMapper;
import com.library.mapper.BookingMapper;
import com.library.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.library.dto.booking.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @InjectMocks
    private BookingService bookingService;

    @Mock
    private UserService userService;

    @Mock
    private ListingService listingService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private BookingCheckoutMapper bookingCheckoutMapper;

//    Booking creating tests

    @Nested
    @DisplayName("Create booking")
    class CreateBooking {
        private final UUID listingId = UUID.randomUUID();
        private final LocalDateTime checkInDate = LocalDateTime.now().plusDays(1)
                .with(LocalTime.NOON);
        private final LocalDateTime checkOutDate = LocalDateTime.now().plusDays(3)
                .with(LocalTime.NOON);
        private final PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        private final String currency = "USD";
        private final String stripeId = "pi_testtesttesttesttesttest";

        private BookingCheckoutRequest createRequest() {
            BookingCheckoutRequest ret = new BookingCheckoutRequest();
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentMethod(paymentMethod);
            paymentRequest.setCurrency(currency);
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setListingId(listingId);
            bookingRequest.setCheckInDate(checkInDate);
            bookingRequest.setCheckOutDate(checkOutDate);

            ret.setBookingRequest(bookingRequest);
            ret.setPaymentRequest(paymentRequest);
            return ret;
        }

        @Test
        @DisplayName("valid request should return bookingCheckoutResponse")
        void validRequest_ReturnsBookingCheckoutResponse() {
            BookingCheckoutRequest bookingCheckoutRequest = createRequest();
            String email = "guest@gmail.com";
            User user = new User();
            user.setEmail(email);

            Listing listing = new Listing();
            listing.setPricePerNight(BigDecimal.valueOf(100));
            User host = new User();
            host.setEmail("host@gmail.com");
            listing.setUser(host);

            BookingResponse expectedResponse = new BookingResponse();
            expectedResponse.setListingId(listingId);
            expectedResponse.setCheckInDate(checkInDate);
            expectedResponse.setCheckOutDate(checkOutDate);

            BookingCheckoutResponse expectedCheckoutResponse = new BookingCheckoutResponse();
            expectedCheckoutResponse.setBookingResponse(expectedResponse);
            expectedCheckoutResponse.setStripeId(stripeId);

            when(userService.getCurrentUserEmail()).thenReturn(email);
            when(userService.getUserByEmail(email)).thenReturn(user);
            when(listingService.getListingDetailedOrThrow(listingId)).thenReturn(listing);
            when(bookingRepository.isListOccupied(any(), any(), any())).thenReturn(false);
            when(paymentService.initializePayment(any(Booking.class), any(PaymentRequest.class)))
                    .thenReturn(stripeId);
            when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(expectedResponse);
            when(bookingCheckoutMapper.toBookingCheckoutResponse(expectedResponse, stripeId))
                    .thenReturn(expectedCheckoutResponse);

            BookingCheckoutResponse bookingCheckoutResponse = bookingService
                    .createBooking(bookingCheckoutRequest);

            ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(bookingCaptor.capture());

            Booking booking = bookingCaptor.getValue();
            assertThat(booking.getCheckInDate()).isEqualTo(checkInDate);
            assertThat(booking.getCheckOutDate()).isEqualTo(checkOutDate);

            assertThat(bookingCheckoutResponse).isNotNull();
            assertThat(bookingCheckoutResponse.getBookingResponse()).isEqualTo(expectedResponse);
            assertThat(bookingCheckoutResponse.getStripeId()).isEqualTo(expectedCheckoutResponse.getStripeId());
        }

        @Test
        @DisplayName("CheckOut before checkIn should throw IllegalStateException")
        void checkOutBeforeCheckIn_ThrowsIllegalStateException() {
            BookingCheckoutRequest bookingCheckoutRequest = createRequest();
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(3));
            bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(2));
            bookingCheckoutRequest.setBookingRequest(bookingRequest);

            assertThatThrownBy(() -> bookingService.createBooking(bookingCheckoutRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid date range");

            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("Check in in the past should throw IllegalStateException")
        void checkInInPast_ThrowsIllegalStateException() {
            BookingCheckoutRequest bookingCheckoutRequest = createRequest();
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setCheckInDate(LocalDateTime.of(2012, 12, 12, 12, 12));
            bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(3).with(LocalTime.NOON));
            bookingCheckoutRequest.setBookingRequest(bookingRequest);

            assertThatThrownBy(() -> bookingService.createBooking(bookingCheckoutRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Check-in in the past");

            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("User trying to book his own listing should throw IllegalStateException")
        void UserIsListingOwner_shouldThrowIllegalStateException() {
            BookingCheckoutRequest bookingCheckoutRequest = createRequest();
            String email = "guest@gmail.com";

            User user = new User();
            user.setEmail(email);

            Listing listing = new Listing();
            listing.setUser(user);

            when(userService.getCurrentUserEmail()).thenReturn(email);
            when(userService.getUserByEmail(email)).thenReturn(user);
            when(listingService.getListingDetailedOrThrow(listingId)).thenReturn(listing);

            assertThatThrownBy(() -> bookingService.createBooking(bookingCheckoutRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You cannot book your own listing!");

            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("Booking is already occupied should throw IllegalStateException")
        void listingIsOccupied_ShouldThrowIllegalStateException() {
            BookingCheckoutRequest bookingCheckoutRequest = createRequest();

            String email = "guest@gmail.com";
            User user = new User();
            user.setEmail(email);

            User user2 = new User();
            user2.setEmail("host@gmail.com");

            Listing listing = new Listing();
            listing.setUser(user2);

            when(userService.getCurrentUserEmail()).thenReturn(email);
            when(userService.getUserByEmail(email)).thenReturn(user);
            when(bookingRepository.isListOccupied(any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> bookingService.createBooking(bookingCheckoutRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Listing is already occupied");
        }
    }

    @Nested
    @DisplayName("Cancel booking")
    class CancelBooking {
        private final UUID bookingId = UUID.randomUUID();

        @Test
        @DisplayName("valid request should cancel booking response")
        void cancelBooking_ValidUserAndPendingStatus_ReturnsCancelledBookingResponse() {
            String email = "test@gmail.com";
            UUID bookingId = UUID.randomUUID();

            User user = new User();
            user.setEmail(email);

            Payment payment = new Payment();
            payment.setStatus(PaymentStatus.COMPLETED);

            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now());
            booking.setBookingId(bookingId);
            booking.setUser(user);
            booking.setPayment(payment);
            booking.setStatus(Status.PENDING);
            booking.setCheckInDate(LocalDateTime.now().plusDays(2));

            BookingResponse bookingResponse = new BookingResponse();
            bookingResponse.setStatus(Status.CANCELLED);

            when(userService.getCurrentUserEmail()).thenReturn(email);
            when(bookingRepository.findDetailedForCancelById(bookingId)).thenReturn(Optional.of(booking));

            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(bookingMapper.toBookingResponse(any())).thenReturn(bookingResponse);
            BookingResponse result = bookingService.cancelBooking(bookingId);

            assertNotNull(result);

            assertEquals(Status.CANCELLED, result.getStatus());

            verify(bookingMapper).toBookingResponse(any(Booking.class));

            verify(bookingRepository).save(argThat(b -> b.getStatus() == Status.CANCELLED));
        }

        @Test
        @DisplayName("Booking doesn't exists should throw entityNotFoundException")
        void cancelBooking_BookingDoesNotExist_ThrowsEntityNotFoundException() {
            UUID id = UUID.randomUUID();
            when(bookingRepository.findDetailedForCancelById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancelBooking(id))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Booking not found with id:" + id);

            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("User isn't owner should throw IllegalStateException")
        void cancelBooking_UserIsNotOwner_ThrowsIllegalStateException() {
            String email = "hacker@gmail.com";
            UUID bookingId = UUID.randomUUID();

            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now());
            booking.setStatus(Status.PENDING);

            User user =  new User();
            user.setEmail("test@gmail.com");

            User owner = new User();
            owner.setEmail("owner@gmail.com");

            Listing listing = new Listing();
            listing.setUser(owner);

            booking.setListing(listing);
            booking.setUser(user);

            when(userService.getCurrentUserEmail()).thenReturn(email);
            when(bookingRepository.findDetailedForCancelById(bookingId)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                    .isInstanceOf(ForbiddenUserException.class)
                    .hasMessage("You is not owner of booking");

            verify(bookingRepository, never()).save(any(Booking.class));

        }

        @Test
        @DisplayName("Booking is already canceled should throw IllegalStateException")
        void cancelBooking_AlreadyCancelled_ThrowsIllegalStateException() {
            String email = "test@gmail.com";
            UUID bookingId = UUID.randomUUID();

            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now());
            booking.setBookingId(bookingId);
            booking.setStatus(Status.CANCELLED);

            User user =  new User();
            user.setEmail(email);

            User owner = new User();
            owner.setEmail("owner@gmail.com");

            Listing listing = new Listing();
            listing.setUser(owner);

            booking.setListing(listing);
            booking.setUser(user);

            when(userService.getCurrentUserEmail()).thenReturn(email);
            when(bookingRepository.findDetailedForCancelById(bookingId)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Booking is already cancelled");

            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("Is took late to cancel booking should throw IllegalStateException")
        void cancelBooking_TooLateToCancel_ThrowsIllegalStateException() {
            String email = "test@gmail.com";

            User user = new User();
            user.setEmail(email);

            User owner = new User();
            owner.setEmail("owner@gmail.com");

            Listing listing = new Listing();
            listing.setUser(owner);

            UUID bookingId = UUID.randomUUID();

            Booking booking = new Booking();
            booking.setBookingId(bookingId);
            booking.setStatus(Status.PENDING);
            booking.setUser(user);
            booking.setListing(listing);
            booking.setCreatedAt(LocalDateTime.now().minusDays(2));

            booking.setCheckInDate(LocalDateTime.now().plusDays(2));
            booking.setCheckOutDate(LocalDateTime.now().plusDays(4));

            when(userService.getCurrentUserEmail()).thenReturn(email);
            when(bookingRepository.findDetailedForCancelById(bookingId)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Too late to cancel booking");

            verify(bookingRepository, never()).save(any(Booking.class));
        }
    }

    @Nested
    @DisplayName("Get booking")
    class GetBooking {
        @Test
        @DisplayName("Valid id should return BookingResponse")
        void getBookingById_ExistingId_ReturnsBookingResponse() {
            UUID bookingId = UUID.randomUUID();
            Booking booking = new Booking();
            booking.setBookingId(bookingId);

            BookingResponse bookingResponse = new BookingResponse();

            when(bookingRepository.findByDetailedId(bookingId)).thenReturn(Optional.of(booking));
            when(bookingMapper.toBookingResponse(any())).thenReturn(bookingResponse);

            BookingResponse actualResponse = bookingService.getBookingById(bookingId);
            assertThat(actualResponse).isEqualTo(bookingResponse);
        }

        @Test
        @DisplayName("Non-existing booking should throw EntityNotFoundException")
        void getBookingById_NonExistingId_ThrowsEntityNotFoundException() {
            UUID bookingId = UUID.randomUUID();
            when(bookingRepository.findByDetailedId(bookingId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> bookingService.getBookingById(bookingId)).isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Booking not found with id: " + bookingId);
        }

//        @Test
//        @DisplayName("User get his bookings should return list of BookingResponse")
//        void getMyBookings_UserHasBookings_ReturnsList(){
//            String email = "test@gmail.com";
//
//            User user = new User();
//            user.setEmail(email);
//
//            Booking booking = new Booking();
//            booking.setUser(user);
//            List<Booking> bl = new ArrayList<>();
//            Pageable pageable = PageRequest.of(0 ,10);
//            bl.add(booking);
//
//            Page<Booking> bp = new PageImpl<>(bl, pageable, bl.size());
//            BookingResponse bookingResponse = new BookingResponse();
//
//            when(userService.getCurrentUserEmail()).thenReturn(email);
//            when(bookingMapper.toBookingResponse(booking)).thenReturn(bookingResponse);
//            when(bookingRepository.findUserBookings()).thenReturn(bp);
//
//            Page<BookingResponse> result = bookingService.getMyBookings(pageable);
//
//            assertThat(result.getContent().get(0)).isEqualTo(bookingResponse);
//            verify(bookingRepository, times(1)).findUserBookings(email, pageable);
//        }

        //    getListingBookings success / failure
        @Test
        @DisplayName("get bookings for listing should return list of BookingResponse")
        void getListingBookings_ListingHasBookings_ReturnsList() {
            UUID listingId = UUID.randomUUID();

            Booking booking = new Booking();

            BookingResponse bookingResponse = new BookingResponse();
            List<Booking> bl = new ArrayList<>();
            bl.add(booking);

            Pageable pageable = PageRequest.of(0 ,10);
            Page<Booking> brp = new PageImpl<>(bl, pageable, bl.size());

            when(bookingRepository.findListingBookingsById(listingId, pageable)).thenReturn(brp);
            when(bookingMapper.toBookingResponse(booking)).thenReturn(bookingResponse);

            Page<BookingResponse> result = bookingService.getListingBookings(listingId, pageable);

            assertThat(result.getContent().getFirst()).isEqualTo(bookingResponse);

            verify(bookingRepository, times(1)).findListingBookingsById(listingId, pageable);
        }
    }

    @Nested
    @DisplayName("isAvailable")
    class bookingIsAvailable {

        @Test
        @DisplayName("valid request should return true (booking is available)")
        void isAvailable_DatesAreFree_ReturnsTrue() {
            UUID listingId = UUID.randomUUID();
            LocalDateTime in =  LocalDateTime.now().plusDays(3);
            LocalDateTime out =  LocalDateTime.now().plusDays(5);

            when(bookingRepository.isListOccupied(listingId, in, out)).thenReturn(false);

            boolean result = bookingService.isAvailable(listingId, in, out);

            assertThat(result).isTrue();
            verify(bookingRepository).isListOccupied(listingId, in, out);
        }

        @Test
        @DisplayName("valid request should return false (booking isn't available)")
        void isAvailable_DatesAreOccupied_ReturnsFalse() {
            UUID listingId = UUID.randomUUID();
            LocalDateTime in =  LocalDateTime.now().plusDays(3);
            LocalDateTime out =  LocalDateTime.now().plusDays(5);

            when(bookingRepository.isListOccupied(listingId, in, out)).thenReturn(true);

            boolean result = bookingService.isAvailable(listingId, in, out);

            assertThat(result).isFalse();
            verify(bookingRepository).isListOccupied(listingId, in, out);
        }
    }
}
