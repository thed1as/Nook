package com.library.service;

import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.entity.Booking;
import com.library.entity.Listing;
import com.library.entity.User;
import com.library.enums.Status;
import com.library.mapper.BookingMapper;
import com.library.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ListingService listingService;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest, UUID userId) {
        if(!bookingRequest.getCheckOutDate().isAfter(bookingRequest.getCheckInDate())) {
            throw new IllegalStateException("Invalid date range");
        }

        if(bookingRequest.getCheckInDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Check-in in the past");
        }

        User user = userService.getUserOrThrow(userId);
        Listing listing = listingService.getListingOrThrow(bookingRequest.getListingId());

        if(bookingRepository.isListOccupied(
                bookingRequest.getListingId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate())){
            throw new IllegalStateException("Listing is already occupied");
        }
        Long days = ChronoUnit.DAYS.between(
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate());

        BigDecimal totalPrice = listing.getPricePerNight().multiply(BigDecimal.valueOf(days));

        Booking booking = new Booking();
        booking.setCheckInDate(bookingRequest.getCheckInDate());
        booking.setCheckOutDate(bookingRequest.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(Status.PENDING);

        user.addBooking(booking);
        listing.addBooking(booking);

        bookingRepository.save(booking);

        return bookingMapper.toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(BookingRequest bookingRequest, UUID userId) {
        Booking booking = bookingRepository.findByListingIdAndUserId(bookingRequest.getListingId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id:"
                        + bookingRequest.getListingId()));
        if(LocalDateTime.now().isAfter(booking.getCheckInDate().minusDays(2)) &&
                booking.getStatus().equals(Status.PENDING)) {
            throw new IllegalStateException("Too late to cancel booking");
        }
        booking.setStatus(Status.CANCELLED);
        bookingRepository.save(booking);
//        back the payment
        return bookingMapper.toBookingResponse(booking);
    }

    public BookingResponse getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .map(bookingMapper::toBookingResponse)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id:"));
    }

    public List<BookingResponse> getUserBookings(UUID userId) {
        return bookingRepository.findUserBookingsById(userId)
                .stream().map(bookingMapper::toBookingResponse).collect(Collectors.toList());
    }

    public boolean isAvailable(UUID listingId, LocalDateTime in, LocalDateTime out) {
        return !bookingRepository.isListOccupied(listingId, in, out);
    }
}
