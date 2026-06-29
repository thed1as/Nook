package com.nooki.service.BookingServices;

import com.nooki.dto.booking.BookingRequest;
import com.nooki.dto.exception.customException.bookingException.BookingIllegalStateException;
import com.nooki.dto.exception.customException.bookingException.BookingNotFoundException;
import com.nooki.dto.exception.customException.listingException.ListingOccupiedException;
import com.nooki.dto.payment.PaymentRequest;
import com.nooki.entity.Booking;
import com.nooki.entity.Listing;
import com.nooki.entity.User;
import com.nooki.enums.Status;
import com.nooki.repository.BookingRepository;
import com.nooki.repository.ListingRepository;
import com.nooki.repository.UserRepository;
import com.nooki.service.CurrencyService;
import com.nooki.service.PaymentServices.PaymentService;
import com.nooki.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingTransactionService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final PaymentService paymentService;
    private final CurrencyService currency;


    private BigDecimal getFullPrice(LocalDateTime checkInDate, LocalDateTime checkOutDate, BigDecimal pricePerNight,
                                    String fromCurrency, String toCurrency) {
        long days = ChronoUnit.DAYS.between(
                checkInDate.toLocalDate(), checkOutDate.toLocalDate());

        if(days <= 0) {
            log.warn("User tried to book less period than 1 night");
            throw new BookingIllegalStateException("Minimum booking period is 1 night");
        }

        BigDecimal baseTotalPrice = pricePerNight.multiply(BigDecimal.valueOf(days));

        return currency.convert(baseTotalPrice, fromCurrency, toCurrency);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Booking reserveBooking(BookingRequest bookingRequest, String toCurrency) {

        Listing lockedListing = listingRepository.findByIdWithLock(bookingRequest.getListingId())
                .orElseThrow(() -> new BookingNotFoundException("Listing not found"));


        Listing detailedListing = listingRepository.findByDetailedId(bookingRequest.getListingId()).get();
        UUID userId = userService.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        if(detailedListing.getUser().getUserId().equals(userId)) {
            log.warn("User with {} id tried to book his listing {}", userId, detailedListing.getListingId());
            throw new BookingIllegalStateException("You cannot book your own listing");
        }

        BigDecimal totalPrice = getFullPrice(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), detailedListing.getPricePerNight(), detailedListing.getCurrency(), toCurrency);

        Booking booking = new Booking();
        booking.setCheckInDate(bookingRequest.getCheckInDate());
        booking.setCheckOutDate(bookingRequest.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setCurrency(toCurrency);
        booking.setStatus(Status.PENDING);

        user.addBooking(booking);
        detailedListing.addBooking(booking);

        try {
            bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            log.error("Conflict: User {} tried to book listing with id {} but already occupied", userId,
                    bookingRequest.getListingId(), e);
            throw new ListingOccupiedException("Listing is already occupied");
        }

        log.info("User: {} reserved listing: {}", userId, bookingRequest.getListingId());
        return booking;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String updateStripeId(UUID bookingId, PaymentRequest paymentRequest) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BookingNotFoundException("Booking not found")
        );
        return paymentService.initializePayment(booking, paymentRequest);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failedBooking(UUID bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking-> {
            booking.setStatus(Status.CANCELLED);
            bookingRepository.save(booking);
        });
        log.info("Booking {} was marked as CANCELLED due to initialization failure", bookingId);    }
}
