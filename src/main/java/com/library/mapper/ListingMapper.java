package com.library.mapper;

import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.dto.listing.UpdateListingRequest;
import com.library.entity.Listing;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {LocationMapper.class, ReviewMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ListingMapper {
    @Mapping(target = "listingTitle", source = "title")
    @Mapping(target = "listingDescription", source = "description")
    @Mapping(target = "listingImages", source = "listingImages")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "reviews", source = "reviews")
    @Mapping(target = "pricePerNight", source = "pricePerNight")
    ListingResponse toListingResponse(Listing listing);

    @Mapping(target = "listingId", ignore = true)
    Listing toListing(ListingRequest listingRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateListing(UpdateListingRequest req, @MappingTarget Listing listing);
}
