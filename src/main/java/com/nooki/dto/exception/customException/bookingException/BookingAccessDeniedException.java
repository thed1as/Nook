package com.nooki.dto.exception.customException.bookingException;

import com.nooki.dto.exception.customException.listingException.ListingException;

public class BookingAccessDeniedException extends ListingException {
    public BookingAccessDeniedException(String message) {
        super(message);
    }
}
