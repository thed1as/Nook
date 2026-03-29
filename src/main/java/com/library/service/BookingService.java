package com.library.service;

import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.dto.user.UserResponse;
import com.library.entity.Booking;
import com.library.entity.Listing;
import com.library.entity.User;
import com.library.enums.Status;
import com.library.mapper.BookingMapper;
import com.library.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ListingService listingService;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest, UUID userId) {
        if(bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
            throw new IllegalArgumentException("check out date must be after check in date ");
        }
        User user = userService.getUserOrThrow(userId);
        Listing listing = listingService.getListingOrThrow(bookingRequest.getListingId());
        if(bookingRepository.isListOccupied(bookingRequest.getListingId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate())) {
            throw new IllegalStateException("Booking is already occupied");
        }
        Long days = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
        BigDecimal totalPrice = listing.getPricePerNight().multiply(BigDecimal.valueOf(days));

        Booking booking = new Booking();
        booking.setCheckInDate(bookingRequest.getCheckInDate());
        booking.setCheckOutDate(bookingRequest.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(Status.PENDING);
        booking.setUser(user);
        booking.setListing(listing);

        return bookingMapper.toBookingResponse(booking);
    }
}
