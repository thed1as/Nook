package com.nooki.mapper;

import com.nooki.dto.ListingImage.ListingImageRequest;
import com.nooki.dto.ListingImage.ListingImageResponse;
import com.nooki.entity.ListingImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListingImageMapper {
    ListingImageResponse toListingImageResponse(ListingImage listingImg);

    ListingImage toListingImage(ListingImageRequest listingRequest);
}
