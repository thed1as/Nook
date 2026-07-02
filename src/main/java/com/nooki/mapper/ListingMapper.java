package com.nooki.mapper;

import com.nooki.dto.ListingImage.ListingImageResponse;
import com.nooki.dto.listing.*;
import com.nooki.entity.Listing;
import com.nooki.entity.ListingImage;
import com.nooki.entity.Review;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {LocationMapper.class, ReviewMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ListingMapper {
    @Mapping(target = "listingId", source = "listingId")
    @Mapping(target = "listingTitle", source = "title")
    @Mapping(target = "listingDescription", source = "description")
    @Mapping(target = "listingImages", source = "listingImages")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "pricePerNight", source = "pricePerNight")
    @Mapping(target = "currency", source = "currency")
    ListingResponse toListingResponse(Listing listing);


    @Mapping(target = "listingId", source = "listing.listingId")
    @Mapping(target = "listingTitle", source = "listing.title")
    @Mapping(target = "listingDescription", source = "listing.description")
    @Mapping(target = "listingImages", source = "listing.listingImages")
    @Mapping(target = "location", source = "listing.location")
    @Mapping(target = "reviewsCount", source = "listing.reviewsCount")
    @Mapping(target = "averageRating", source = "listing.averageRating")
    @Mapping(target = "pricePerNight", source = "listing.pricePerNight")
    @Mapping(target = "reviews", source = "top3")
    FullListingResponse toFullListingResponse(Listing listing, List<Review> top3);

    @Mapping(target = "previewImage", source = "listingImages", qualifiedByName = "mapFirstImage")
    @Mapping(target = "listingTitle", source = "title")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "averageRating", source = "averageRating")
    @Mapping(target = "reviewsCount", source = "reviewsCount")
    ShortListingResponse toShortListingResponse(Listing listing);

    @Named("mapFirstImage")
    default ListingImageResponse mapFirstImage(List<ListingImage> listingImages) {
        if(listingImages == null || listingImages.isEmpty()) {
            return null;
        }
        ListingImage first = listingImages.getFirst();
        return new ListingImageResponse(first.getFileName());
    }


    @Mapping(target = "listingId", ignore = true)
    Listing toListing(ListingRequest listingRequest);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateListing(UpdateListingRequest req, @MappingTarget Listing listing);
}
