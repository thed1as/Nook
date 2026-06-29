package com.nooki.dto.exception.customException.listingException;

public class ListingCancelException extends ListingException {
    public ListingCancelException(String message) {
        super(message);
    }

    public ListingCancelException(String message, Throwable cause) {
        super(message, cause);
    }
}
