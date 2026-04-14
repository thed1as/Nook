package com.library.mapper;

import com.library.dto.ListingImage.ListingImageRequest;
import com.library.dto.ListingImage.ListingImageResponse;
import com.library.entity.ListingImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListingImageMapper {
    ListingImageResponse toListingImageResponse(ListingImage listingImg);

    ListingImage toListingImage(ListingImageRequest listingRequest);
}
