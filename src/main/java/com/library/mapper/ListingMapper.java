package com.library.mapper;

import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.entity.Listing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ListingMapper {
    ListingResponse toListingResponse(Listing listing);

    @Mapping(target = "id", ignore = true)
    Listing toListing(ListingResponse listingResponse);

    @Mapping(target = "id", ignore = true)
    Listing toListing(ListingRequest listingRequest);
}
