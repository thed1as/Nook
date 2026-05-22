package com.library.dto.exception.customException.paymentExceptions;

public class RefundNotAllowedException extends PaymentException {
    public RefundNotAllowedException(String message) {
        super(message);
    }
}
