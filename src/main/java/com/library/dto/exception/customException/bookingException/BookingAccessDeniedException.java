package com.library.dto.exception.customException.bookingException;

import com.library.dto.exception.customException.listingException.ListingException;

public class BookingAccessDeniedException extends ListingException {
    public BookingAccessDeniedException(String message) {
        super(message);
    }
}
