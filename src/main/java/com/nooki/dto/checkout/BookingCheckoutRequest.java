package com.nooki.dto.checkout;

import com.nooki.dto.booking.BookingRequest;
import com.nooki.dto.payment.PaymentRequest;
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
