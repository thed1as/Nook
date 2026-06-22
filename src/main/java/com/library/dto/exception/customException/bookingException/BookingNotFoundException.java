package com.library.dto.exception.customException.bookingException;

import com.library.dto.exception.customException.paymentExceptions.PaymentException;

public class BookingNotFoundException extends PaymentException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
