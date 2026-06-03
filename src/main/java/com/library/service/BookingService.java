package com.library.service;

import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.dto.checkout.BookingCheckoutRequest;
import com.library.dto.checkout.BookingCheckoutResponse;
import com.library.dto.exception.customException.forbiden.ForbiddenUserException;
import com.library.entity.Booking;
import com.library.entity.Listing;
import com.library.entity.Payment;
import com.library.entity.User;
import com.library.enums.PaymentStatus;
import com.library.enums.Status;
import com.library.mapper.BookingCheckoutMapper;
import com.library.mapper.BookingMapper;
import com.library.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final PaymentService paymentService;
    private final BookingMapper bookingMapper;
    private final BookingCheckoutMapper bookingCheckoutMapper;
    private final BookingTransactionService txService;

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

    public BookingCheckoutResponse createBooking(BookingCheckoutRequest bookingCheckinRequest) {
        BookingRequest bookingRequest = bookingCheckinRequest.getBookingRequest();
        if(bookingRepository.isListOccupied(
                bookingRequest.getListingId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate()
        )) {
            throw new IllegalStateException("Listing is already occupied");
        }


        String email = userService.getCurrentUserEmail();
        if(!bookingRequest.getCheckOutDate().isAfter(bookingRequest.getCheckInDate())) {
            throw new IllegalStateException("Invalid date range");
        }

        Booking pendingBooking = txService.reserveBooking(bookingRequest, email);

        String stripeId;
        try {
            stripeId = paymentService.initializePayment(pendingBooking, bookingCheckinRequest.getPaymentRequest());
        } catch (Exception ex) {
            txService.failedBooking(pendingBooking.getBookingId());
            throw new RuntimeException("payment initialize failed", ex);
        }

        return bookingCheckoutMapper.toBookingCheckoutResponse(
                bookingMapper.toBookingResponse(pendingBooking), stripeId
        );
    }


    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        String email = userService.getCurrentUserEmail();
        Booking booking = bookingRepository.findDetailedForCancelById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id:"
                        + bookingId));
        if(!booking.getUser().getEmail().equals(email) &&
                !booking.getListing().getUser().getEmail().equals(email)) {
            throw new ForbiddenUserException("You is not owner of booking");
        }

        if(booking.getStatus().equals(Status.CANCELLED)) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if((LocalDateTime.now().isAfter(booking.getCheckInDate().minusDays(cancellationWindowDays)) ||
                LocalDateTime.now().isAfter(booking.getCreatedAt().plusDays(1))) &&
                booking.getStatus().equals(Status.PENDING)) {
            throw new IllegalStateException("Too late to cancel booking");
        }
        booking.setStatus(Status.CANCELLED);
        bookingRepository.save(booking);

        Payment payment = booking.getPayment();
        if(payment.getStatus().equals(PaymentStatus.COMPLETED)) {
            paymentService.processRefundForCancellationBooking(payment);
        }

        return bookingMapper.toBookingResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId) {
        return bookingRepository.findByDetailedId(bookingId)
                .map(bookingMapper::toBookingResponse)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(Pageable pageable) {
        String email = userService.getCurrentUserEmail();

        Page<UUID> ids = bookingRepository.findAllIdsOfUser(pageable, email);

        if(ids.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Booking> bookings = bookingRepository.findUserBookings(ids.getContent());
        return new PageImpl<>(bookings, pageable, bookings.size()).map(bookingMapper::toBookingResponse);
    }

    @Transactional
    public Page<BookingResponse> getListingBookings(UUID listingId, Pageable pageable) {
//        show only if user have booking or its owner checking his listing bookings
        return bookingRepository.findListingBookingsById(listingId, pageable).map(bookingMapper::toBookingResponse);
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(UUID listingId, LocalDateTime in, LocalDateTime out) {
        return !bookingRepository.isListOccupied(listingId, in, out);
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime trashold = LocalDateTime.now().minusDays(24);
    }
}
