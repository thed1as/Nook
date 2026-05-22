package com.library.dto.exception.customException.paymentExceptions;

public class PaymentAlreadyExistsException extends PaymentException {
    public PaymentAlreadyExistsException(String message) {
        super(message);
    }
}
