package com.library.service;

import com.library.dto.booking.BookingRequest;
import com.library.dto.payment.PaymentRequest;
import com.library.entity.Booking;
import com.library.entity.Listing;
import com.library.entity.User;
import com.library.enums.Status;
import com.library.repository.BookingRepository;
import com.library.repository.ListingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

// RESERVESPOT() TO CHECK EVERYTHING FIRST

// UPDATESTRIPEID()

// failBOOKING

// getfullPrice

@Service
@RequiredArgsConstructor
public class BookingTransactionService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ListingRepository listingRepository;
    private final PaymentService paymentService;


    private BigDecimal getFullPrice(LocalDateTime checkInDate, LocalDateTime checkOutDate, BigDecimal pricePerNight) {
        long days = ChronoUnit.DAYS.between(
                checkInDate.toLocalDate(), checkOutDate.toLocalDate());

        if(days <= 0) {
            throw new IllegalStateException("Minimum booking period is 1 night");
        }

        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Booking reserveBooking(BookingRequest bookingRequest, String email) {

        Listing lockedListing = listingRepository.findByIdWithLock(bookingRequest.getListingId())
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));

        Listing detailedListing = listingRepository.findByDetailedId(bookingRequest.getListingId()).get();
        User user = userService.getUserByEmail(email);

        if(detailedListing.getUser().equals(user)) {
            throw new IllegalStateException("You cannot book your own listing");
        }

        BigDecimal totalPrice = getFullPrice(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), detailedListing.getPricePerNight());

        Booking booking = new Booking();
        booking.setCheckInDate(bookingRequest.getCheckInDate());
        booking.setCheckOutDate(bookingRequest.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(Status.PENDING);

        user.addBooking(booking);
        detailedListing.addBooking(booking);

        return bookingRepository.saveAndFlush(booking);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String updateStripeId(UUID bookingId, PaymentRequest paymentRequest) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Booking not found")
        );
        return paymentService.initializePayment(booking, paymentRequest);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failedBooking(UUID bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking-> {
            booking.setStatus(Status.CANCELLED);
            bookingRepository.save(booking);
        });
    }
}
