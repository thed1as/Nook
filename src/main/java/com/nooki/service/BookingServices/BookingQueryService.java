package com.nooki.service.BookingServices;

import com.nooki.dto.booking.BookingResponse;
import com.nooki.dto.exception.customException.bookingException.BookingNotFoundException;
import com.nooki.entity.Booking;
import com.nooki.mapper.BookingMapper;
import com.nooki.repository.BookingRepository;
import com.nooki.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingQueryService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final BookingMapper bookingMapper;

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId) {
        return bookingRepository.findByDetailedId(bookingId)
                .map(bookingMapper::toBookingResponse)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(Pageable pageable) {
        UUID userId = userService.getCurrentUserId();

        Page<UUID> ids = bookingRepository.findAllIdsOfUser(pageable, userId);

        if(ids.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Booking> bookings = bookingRepository.findUserBookings(ids.getContent());
        return new PageImpl<>(bookings, pageable, bookings.size()).map(bookingMapper::toBookingResponse);
    }

    @Transactional
    public Page<BookingResponse> getListingBookings(UUID listingId, Pageable pageable) {
        return bookingRepository.findListingBookingsById(listingId, pageable).map(bookingMapper::toBookingResponse);
    }
}
