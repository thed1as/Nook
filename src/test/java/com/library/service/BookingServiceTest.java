package com.library.service;

import com.library.dto.booking.BookingRequest;
import com.library.entity.Booking;
import com.library.entity.Listing;
import com.library.entity.User;
import com.library.enums.Status;
import com.library.mapper.BookingMapper;
import com.library.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.library.dto.booking.BookingResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

//    Booking creating tests

    @Test
    void createBooking_ValidRequest_ReturnsBookingResponse() {
        String email = "test@gmail.com";

        BookingRequest bookingRequest = new BookingRequest();
        UUID bookingId = UUID.randomUUID();
        bookingRequest.setListingId(bookingId);
        bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(2));
        bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(3));

        User user = new User();
        user.setEmail(email);

        User owner = new User();
        owner.setEmail("owner@gmail.com");

        Listing listing = new Listing();
        listing.setPricePerNight(BigDecimal.valueOf(25000));
        listing.setUser(owner);

        Booking booking = new Booking();
        BookingResponse response = new BookingResponse();

        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(listingService.getListingOrThrow(bookingId)).thenReturn(listing);
        when(bookingRepository.isListOccupied(any(), any(), any())).thenReturn(false);
        when(bookingMapper.toBookingResponse(any())).thenReturn(response);

        BookingResponse result = bookingService.createBooking(bookingRequest);

        assertThat(result).isNotNull();

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

//    Invalid date range
    @Test
    void createBooking_CheckOutBeforeCheckIn_ThrowsIllegalStateException() {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(3));
        bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalStateException.class).hasMessage("Invalid date range");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_CheckInDateInPast_ThrowsIllegalStateException(){
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setCheckInDate(LocalDateTime.now().minusDays(2));
        bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalStateException.class).hasMessage("Check-in in the past");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_UserIsListingOwner_ThrowsIllegalStateException() {
        String email = "test@gmail.com";

        BookingRequest bookingRequest = new BookingRequest();
        UUID bookingId = UUID.randomUUID();
        bookingRequest.setListingId(bookingId);
        bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(2));
        bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(4));

        User user = new User();
        user.setEmail(email);

        Listing listing = new Listing();
        listing.setPricePerNight(BigDecimal.valueOf(25000));
        listing.setUser(user);

        Booking booking = new Booking();
        BookingResponse response = new BookingResponse();

        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(listingService.getListingOrThrow(bookingId)).thenReturn(listing);
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalStateException.class).hasMessage("You cannot book your own listing!");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_ListingIsOccupied_ThrowsIllegalStateException() {
        String email = "test@gmail.com";

        BookingRequest bookingRequest = new BookingRequest();
        UUID bookingId = UUID.randomUUID();
        bookingRequest.setListingId(bookingId);
        bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(2));
        bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(4));

        User user = new User();
        user.setEmail(email);

        User owner = new User();
        owner.setEmail("owner@gmail.com");

        Listing listing = new Listing();
        listing.setPricePerNight(BigDecimal.valueOf(25000));
        listing.setUser(owner);

        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(listingService.getListingOrThrow(bookingId)).thenReturn(listing);
        when(bookingRepository.isListOccupied(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalStateException.class).hasMessage("Listing is already occupied");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_DaysMoreThanOne_IllegalStateException() {
        String email = "test@gmail.com";

        BookingRequest bookingRequest = new BookingRequest();
        UUID bookingId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2026, 10, 10, 12, 0);
        bookingRequest.setCheckInDate(start);
        bookingRequest.setCheckOutDate(start.plusHours(2));
        bookingRequest.setListingId(bookingId);

        User user = new User();
        user.setEmail(email);

        User owner = new User();
        owner.setEmail("owner@gmail.com");

        Listing listing = new Listing();
        listing.setUser(owner);

        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(listingService.getListingOrThrow(bookingId)).thenReturn(listing);
        when(bookingRepository.isListOccupied(any(),any(),any())).thenReturn(false);

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Minimum booking period is 1 night");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    //    Cancelling booking
    @Test
    void cancelBooking_ValidUserAndPendingStatus_ReturnsCancelledBookingResponse() {
        String email = "test@gmail.com";
        UUID bookingId = UUID.randomUUID();

        User user = new User();
        user.setEmail(email);

        Booking booking = new Booking();
        booking.setCreatedAt(LocalDateTime.now());
        booking.setBookingId(bookingId);
        booking.setUser(user);
        booking.setStatus(Status.PENDING);
        booking.setCheckInDate(LocalDateTime.now().plusDays(2));

        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setStatus(Status.CANCELLED);

        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

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
    void cancelBooking_BookingDoesNotExist_ThrowsEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Booking not found with id:" + id);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
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
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You is not owner of booking");

        verify(bookingRepository, never()).save(any(Booking.class));

    }

    @Test
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
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Booking is already cancelled");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
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
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Too late to cancel booking");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

//    gettingBookById success/failure
    @Test
    void getBookingById_ExistingId_ReturnsBookingResponse() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setBookingId(bookingId);

        BookingResponse bookingResponse = new BookingResponse();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponse(any())).thenReturn(bookingResponse);

        BookingResponse actualResponse = bookingService.getBookingById(bookingId);
        assertThat(actualResponse).isEqualTo(bookingResponse);
        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    void getBookingById_NonExistingId_ThrowsEntityNotFoundException() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.getBookingById(bookingId)).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Booking not found with id: " + bookingId);
    }

//    getMyBookings success/failure
    @Test
    void getMyBookings_UserHasBookings_ReturnsList(){
        String email = "test@gmail.com";

        User user = new User();
        user.setEmail(email);

        Booking booking = new Booking();
        booking.setUser(user);

        BookingResponse bookingResponse = new BookingResponse();

        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(bookingMapper.toBookingResponse(booking)).thenReturn(bookingResponse);
        when(bookingRepository.findUserBookingsByEmail(email)).thenReturn(List.of(booking));

        List<BookingResponse> result = bookingService.getMyBookings();

        assertThat(result).isEqualTo(List.of(bookingResponse));
        verify(bookingRepository, times(1)).findUserBookingsByEmail(email);
    }

    @Test
    void getMyBookings_UserHasNoBookings_ReturnsEmptyList(){
        String email = "test@gmail.com";

        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(bookingRepository.findUserBookingsByEmail(email)).thenReturn(Collections.emptyList());

        List<BookingResponse> result = bookingService.getMyBookings();

        assertThat(result).isEqualTo(Collections.emptyList());
        verify(bookingRepository, times(1)).findUserBookingsByEmail(email);
    }

//    getListingBookings success / failure
    @Test
    void getListingBookings_ListingHasBookings_ReturnsList() {
        UUID listingId = UUID.randomUUID();

        Booking booking = new Booking();

        BookingResponse bookingResponse = new BookingResponse();

        when(bookingMapper.toBookingResponse(any())).thenReturn(bookingResponse);
        when(bookingRepository.findListingBookingsById(listingId)).thenReturn(List.of(booking));

        List<BookingResponse> result = bookingService.getListingBookings(listingId);

        assertThat(result).isEqualTo(List.of(bookingResponse));

        verify(bookingRepository, times(1)).findListingBookingsById(listingId);
    }

    @Test
    void getListingBookings_ListingHasNoBookings_ReturnsEmptyList() {
        when(bookingRepository.findListingBookingsById(any())).thenReturn(Collections.emptyList());
        List<BookingResponse> result = bookingService.getListingBookings(any());

        assertThat(result).isEqualTo(Collections.emptyList());

        verify(bookingRepository, times(1)).findListingBookingsById(any());
    }

//    isAvailable true / false

    @Test
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
