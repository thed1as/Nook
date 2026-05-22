package com.library.dto.exception.customException.forbiden;

public class ForbiddenUserException extends RuntimeException {
    public ForbiddenUserException(String message) {
        super(message);
    }
}
