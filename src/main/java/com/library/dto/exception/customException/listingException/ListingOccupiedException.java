package com.library.dto.exception.customException.listingException;

public class ListingOccupiedException extends RuntimeException {
    public ListingOccupiedException(String message) {
        super(message);
    }

    public ListingOccupiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
