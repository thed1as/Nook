package com.library.dto.checkout;

import com.library.dto.booking.BookingRequest;
import com.library.dto.payment.PaymentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class BookingCheckoutRequest {
    @NotNull
    @Valid
    private BookingRequest bookingRequest;
    @NotNull
    @Valid
    private PaymentRequest paymentRequest;
}
