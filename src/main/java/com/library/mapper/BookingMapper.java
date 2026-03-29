package com.library.mapper;

import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface                                                                                                                        BookingMapper {
    BookingResponse toBookingResponse(Booking booking);

    @Mapping(target = "id", ignore = true)
    Booking toBooking(BookingResponse bookingResponse);

    @Mapping(target = "id", ignore = true)
    Booking toBooking(BookingRequest bookingRequest);
}
