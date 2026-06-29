package com.nooki.dto.exception.customException.bookingException;

import com.nooki.dto.exception.customException.paymentExceptions.PaymentException;

public class BookingIllegalStateException extends PaymentException {
    public BookingIllegalStateException(String message) {
        super(message);
    }
}
