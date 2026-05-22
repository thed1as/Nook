package com.library.dto.exception.customException.paymentExceptions;

public class PaymentFailedException extends PaymentException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
