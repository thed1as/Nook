package com.library.dto.exception.customException.bookingException;

import com.library.dto.exception.customException.paymentExceptions.PaymentException;

public class BookingCancelException extends PaymentException {
    public BookingCancelException(String message) {
        super(message);
    }
}
