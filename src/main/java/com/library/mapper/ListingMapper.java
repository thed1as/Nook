package com.library.mapper;

import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.dto.listing.UpdateListingRequest;
import com.library.entity.Listing;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ListingMapper {
    ListingResponse toListingResponse(Listing listing);

    @Mapping(target = "listingId", ignore = true)
    Listing toListing(ListingRequest listingRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateListing(UpdateListingRequest req, @MappingTarget Listing listing);
}
