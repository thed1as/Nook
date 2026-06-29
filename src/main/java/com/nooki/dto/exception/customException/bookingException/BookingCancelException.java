package com.nooki.dto.exception.customException.bookingException;

import com.nooki.dto.exception.customException.paymentExceptions.PaymentException;

public class BookingCancelException extends PaymentException {
    public BookingCancelException(String message) {
        super(message);
    }
}
