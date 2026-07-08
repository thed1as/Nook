package com.nooki.service.BookingServices;

import com.nooki.dto.booking.BookingRequest;
import com.nooki.dto.booking.BookingResponse;
import com.nooki.dto.checkout.BookingCheckoutRequest;
import com.nooki.dto.checkout.BookingCheckoutResponse;
import com.nooki.dto.exception.customException.bookingException.BookingAccessDeniedException;
import com.nooki.dto.exception.customException.bookingException.BookingCancelException;
import com.nooki.dto.exception.customException.bookingException.BookingIllegalStateException;
import com.nooki.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.nooki.entity.Booking;
import com.nooki.entity.Payment;
import com.nooki.enums.PaymentStatus;
import com.nooki.enums.Status;
import com.nooki.mapper.BookingCheckoutMapper;
import com.nooki.mapper.BookingMapper;
import com.nooki.repository.BookingRepository;
import com.nooki.service.PaymentServices.PaymentService;
import com.nooki.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;


import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCommandService {
    private final PaymentService paymentService;
    private final BookingMapper bookingMapper;
    private final BookingCheckoutMapper bookingCheckoutMapper;
    private final BookingTransactionService transactionService;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    public BookingCheckoutResponse create(BookingCheckoutRequest request) {
        BookingRequest bookingRequest = request.getBookingRequest();

        if(!bookingRequest.getCheckOutDate().isAfter(bookingRequest.getCheckInDate())){
            log.warn("Validation wrong check-out: {}, check-in: {}", bookingRequest.getCheckOutDate(), bookingRequest.getCheckInDate());
            throw new BookingIllegalStateException("Check-out have to be after check-in");
        }

        Booking pendingBooking = transactionService.reserveBooking(bookingRequest, request.getPaymentRequest().getCurrency());

        String stripeId;
        try {
            stripeId = paymentService.initializePayment(pendingBooking, request.getPaymentRequest());
        } catch (Exception e) {
            transactionService.failedBooking(pendingBooking.getBookingId());
            log.error("Failed to initialize payment", e);
            throw new PaymentFailedException(e.getMessage());
        }

        log.info("Successfully created booking {} for user {} with Stripe Payment ID {}",
                pendingBooking.getBookingId(), userService.getCurrentUserId(), stripeId);

        return bookingCheckoutMapper.toBookingCheckoutResponse(
                bookingMapper.toBookingResponse(pendingBooking),
                stripeId
        );
    }

    @Transactional
    public BookingResponse cancel(UUID bookingId){
        UUID userId = userService.getCurrentUserId();
        Booking booking = bookingRepository.findDetailedForCancelById(bookingId)
                .orElseThrow(() -> new BookingIllegalStateException("Booking not found"));

        if(booking.getStatus().equals(Status.COMPLETED) || booking.getStatus().equals(Status.CANCELLED)){
            log.warn("User with {} id tried to cancel booking with status: {}", userId,  booking.getStatus());
            throw new BookingCancelException("Cannot cancel booking with status: " + booking.getStatus());
        }

        if(!booking.getUser().getUserId().equals(userId) &&
                !booking.getListing().getUser().getUserId().equals(userId)){
            throw new BookingAccessDeniedException("You isn't owner of booking");
        }

        booking.setStatus(Status.CANCELLED);

        Payment payment = booking.getPayment();
        if(payment.getStatus().equals(PaymentStatus.COMPLETED)){
            paymentService.processRefundForCancellationBooking(payment);
        }
        bookingRepository.save(booking);

        log.info("User with {} id successfully cancelled booking with id: {}",  userId, bookingId);
        return bookingMapper.toBookingResponse(booking);
    }

    public void cancelAndRefundAllFutureBookings(UUID listingId) {
        List<UUID> allActiveBooking = bookingRepository.findAllIds(listingId);
        for(UUID in : allActiveBooking){
            try {
                transactionService.cancelBookingBySystem(in);
            } catch (Exception e) {
                log.error("Failed to cancel booking with id {}: {}", in, e.getMessage());
            }
        }
    }

}
