package com.library.dto.exception.customException.bookingException;

import com.library.dto.exception.customException.paymentExceptions.PaymentException;

public class BookingIllegalStateException extends PaymentException {
    public BookingIllegalStateException(String message) {
        super(message);
    }
}
