package com.library.mapper;

import com.library.dto.booking.BookingResponse;
import com.library.dto.checkout.BookingCheckoutResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingCheckoutMapper {

    @Mapping(target = "bookingResponse", source = "bookingResponse")
    @Mapping(target = "stripeId", source = "stripeId")
    BookingCheckoutResponse toBookingCheckoutResponse(BookingResponse bookingResponse, String stripeId);
}
