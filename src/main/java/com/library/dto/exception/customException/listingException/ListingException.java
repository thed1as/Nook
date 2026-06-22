package com.library.dto.exception.customException.listingException;

public class ListingException extends RuntimeException {
    public ListingException(String message) {
        super(message);
    }

    public ListingException(String message, Throwable cause) {
        super(message, cause);
    }
}
