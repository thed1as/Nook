package com.nooki.service.booking;

import com.nooki.dto.booking.BookingRequest;
import com.nooki.dto.exception.customException.bookingException.BookingIllegalStateException;
import com.nooki.dto.exception.customException.bookingException.BookingNotFoundException;
import com.nooki.dto.exception.customException.listingException.ListingOccupiedException;
import com.nooki.entity.Booking;
import com.nooki.entity.Listing;
import com.nooki.entity.User;
import com.nooki.enums.Status;
import com.nooki.repository.BookingRepository;
import com.nooki.repository.ListingRepository;
import com.nooki.repository.UserRepository;
import com.nooki.service.BookingServices.BookingTransactionService;
import com.nooki.service.CurrencyService;
import com.nooki.service.PaymentServices.PaymentService;
import com.nooki.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingTransactionServiceTest {

    @InjectMocks
    private BookingTransactionService bookingTransactionService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private CurrencyService currencyService;

    @Nested
    @DisplayName("Reserve booking test")
    class reserveBooking {
        private final UUID listingId = UUID.randomUUID();
        private final LocalDateTime checkInDate = LocalDateTime.now().plusDays(1)
                .with(LocalTime.NOON);
        private final LocalDateTime checkOutDate = LocalDateTime.now().plusDays(3)
                .with(LocalTime.NOON);
        private final String toCurrency = "EUR";

        private BookingRequest createRequest() {
            BookingRequest br = new BookingRequest();
            br.setListingId(listingId);
            br.setCheckInDate(checkInDate);
            br.setCheckOutDate(checkOutDate);
            return br;
        }

        @Test
        @DisplayName("valid request should return bookingCheckoutResponse")
        void validRequest_returnsBooking(){
            BookingRequest bookingRequest = createRequest();
            Listing detailedListing = new Listing();
            detailedListing.setPricePerNight(BigDecimal.valueOf(100));

            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setUserId(userId);

            User host = new User();
            host.setUserId(UUID.randomUUID());
            detailedListing.setUser(host);

            Booking expectedBooking = new Booking();
            expectedBooking.setCheckInDate(checkInDate);
            expectedBooking.setCheckOutDate(checkOutDate);
            expectedBooking.setCurrency(toCurrency);
            expectedBooking.setStatus(Status.PENDING);

            when(listingRepository.findByIdWithLock(bookingRequest.getListingId())).thenReturn(Optional.of(detailedListing));
            when(listingRepository.findByDetailedId(listingId)).thenReturn(Optional.of(detailedListing));
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.getReferenceById(any(UUID.class))).thenReturn(user);

            Booking result = bookingTransactionService.reserveBooking(bookingRequest, toCurrency);

            assertThat(result).isNotNull();

            assertEquals(result.getCheckInDate(), checkInDate);
            assertEquals(result.getCheckOutDate(), checkOutDate);
            assertEquals(result.getCurrency(), toCurrency);
            assertEquals(result.getStatus().toString(), Status.PENDING.toString());
            verify(bookingRepository, times(1)).save(any(Booking.class));
        }

//        @Test
//        @DisplayName("CheckOut before checkIn should throw IllegalStateException")
//        void checkOutBeforeCheckIn_ThrowsIllegalStateException() {
//            BookingRequest request = createRequest();
//
//            assertThatThrownBy(() -> bookingTransactionService.reserveBooking(request, toCurrency))
//                    .isInstanceOf(IllegalStateException.class)
//                    .hasMessage("Invalid date range");
//
//            verify(bookingRepository, never()).save(any(Booking.class));
//        }

        @Test
        @DisplayName("Check in in the past should throw IllegalStateException")
        void checkInInPast_ThrowsIllegalStateException() {
            BookingRequest bookingRequest = createRequest();

            assertThatThrownBy(() -> bookingTransactionService.reserveBooking(bookingRequest, toCurrency))
                    .isInstanceOf(BookingNotFoundException.class)
                    .hasMessage("Listing not found");

            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("Owner tried to book his listing")
        void triedToBookHisListing_ThrowsIllegalStateException() {
            BookingRequest bookingRequest = createRequest();

            UUID ownerId = UUID.randomUUID();
            User owner = new User();
            owner.setUserId(ownerId);

            Listing listing = new Listing();
            listing.setUser(owner);

            when(listingRepository.findByIdWithLock(listingId)).thenReturn(Optional.of(listing));
            when(listingRepository.findByDetailedId(listingId)).thenReturn(Optional.of(listing));
            when(userService.getCurrentUserId()).thenReturn(ownerId);
            when(userRepository.getReferenceById(any(UUID.class))).thenReturn(owner);

            assertThatThrownBy(() -> bookingTransactionService.reserveBooking(bookingRequest, toCurrency))
                    .isInstanceOf(BookingIllegalStateException.class)
                    .hasMessage("You cannot book your own listing");

            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("Listing occupied due to that booking saving error")
        void savingError_shouldThrowListingOccupiedException() {
            BookingRequest bookingRequest = createRequest();
            Listing detailedListing = new Listing();
            detailedListing.setPricePerNight(BigDecimal.valueOf(100));

            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setUserId(userId);

            User host = new User();
            host.setUserId(UUID.randomUUID());
            detailedListing.setUser(host);

            Booking expectedBooking = new Booking();
            expectedBooking.setCheckInDate(checkInDate);
            expectedBooking.setCheckOutDate(checkOutDate);
            expectedBooking.setCurrency(toCurrency);
            expectedBooking.setStatus(Status.PENDING);

            when(listingRepository.findByIdWithLock(bookingRequest.getListingId())).thenReturn(Optional.of(detailedListing));
            when(listingRepository.findByDetailedId(listingId)).thenReturn(Optional.of(detailedListing));
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.getReferenceById(any(UUID.class))).thenReturn(user);

            when(bookingRepository.save(any(Booking.class))).thenThrow(DataIntegrityViolationException.class);
            assertThatThrownBy(() -> bookingTransactionService.reserveBooking(bookingRequest, toCurrency))
                    .isInstanceOf(ListingOccupiedException.class)
                    .hasMessage("Listing is already occupied");
        }
    }
}
