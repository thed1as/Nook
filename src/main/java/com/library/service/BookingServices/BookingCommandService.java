package com.library.service.BookingServices;

import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.dto.checkout.BookingCheckoutRequest;
import com.library.dto.checkout.BookingCheckoutResponse;
import com.library.dto.exception.customException.bookingException.BookingAccessDeniedException;
import com.library.dto.exception.customException.bookingException.BookingCancelException;
import com.library.dto.exception.customException.bookingException.BookingIllegalStateException;
import com.library.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.library.entity.Booking;
import com.library.entity.Payment;
import com.library.enums.PaymentStatus;
import com.library.enums.Status;
import com.library.mapper.BookingCheckoutMapper;
import com.library.mapper.BookingMapper;
import com.library.repository.BookingRepository;
import com.library.service.PaymentServices.PaymentService;
import com.library.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
