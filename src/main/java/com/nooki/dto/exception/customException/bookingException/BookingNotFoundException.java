package com.nooki.dto.exception.customException.bookingException;

import com.nooki.dto.exception.customException.paymentExceptions.PaymentException;

public class BookingNotFoundException extends PaymentException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
