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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.booking.cancellation-window-days}")
    private long cancellationWindowDays;

    private BigDecimal getFullPrice(LocalDateTime checkInDate, LocalDateTime checkOutDate, BigDecimal pricePerNight) {
        long days = ChronoUnit.DAYS.between(
                checkInDate.toLocalDate(), checkOutDate.toLocalDate());

        if(days <= 0) {
            throw new IllegalStateException("Minimum booking period is 1 night");
        }

        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        String email = userService.getCurrentUserEmail();

        if(!bookingRequest.getCheckOutDate().isAfter(bookingRequest.getCheckInDate())) {
            throw new IllegalStateException("Invalid date range");
        }

        if(bookingRequest.getCheckInDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Check-in in the past");
        }

        User user = userService.getUserByEmail(email);
        Listing listing = listingService.getListingOrThrow(bookingRequest.getListingId());
        if(listing.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("You cannot book your own listing!");
        }

        if(bookingRepository.isListOccupied(
                bookingRequest.getListingId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate())){
            throw new IllegalStateException("Listing is already occupied");
        }

        BigDecimal totalPrice = getFullPrice(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), listing.getPricePerNight());

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
    public BookingResponse cancelBooking(UUID bookingId) {
        String email = userService.getCurrentUserEmail();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id:"
                        + bookingId));
        if(!booking.getUser().getEmail().equals(email) &&
                !booking.getListing().getUser().getEmail().equals(email)) {
            throw new IllegalStateException("You is not owner of booking");
        }
        if(LocalDateTime.now().isAfter(booking.getCheckInDate().minusDays(cancellationWindowDays)) &&
                booking.getStatus().equals(Status.PENDING) &&
                LocalDateTime.now().isAfter(booking.getCreatedAt().plusDays(1))) {
            throw new IllegalStateException("Too late to cancel booking");
        }
        booking.setStatus(Status.CANCELLED);
        bookingRepository.save(booking);
//        back the payment
        return bookingMapper.toBookingResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .map(bookingMapper::toBookingResponse)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        String email = userService.getCurrentUserEmail();
        return bookingRepository.findUserBookingsByEmail(email)
                .stream().map(bookingMapper::toBookingResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<BookingResponse> getListingBookings(UUID listingId) {
        return bookingRepository.findListingBookingsById(listingId)
                .stream().map(bookingMapper::toBookingResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(UUID listingId, LocalDateTime in, LocalDateTime out) {
        return !bookingRepository.isListOccupied(listingId, in, out);
    }
}
