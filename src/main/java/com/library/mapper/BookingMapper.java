package com.library.mapper;

import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, ListingImageMapper.class})
public interface BookingMapper {
    @Mapping(target = "listingId", source = "listing.listingId")
    @Mapping(target = "listingTitle", source = "listing.title")
    @Mapping(target = "listingDescription", source = "listing.description")
    @Mapping(target = "listingImage", source = "listing.listingImages")
    @Mapping(target = "location", source = "listing.location")
    @Mapping(target = "username", source = "user.username")
    BookingResponse toBookingResponse(Booking booking);

    @Mapping(target = "bookingId", ignore = true)
    Booking toBooking(BookingRequest bookingRequest);
}
