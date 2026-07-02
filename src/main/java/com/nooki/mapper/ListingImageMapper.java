package com.nooki.mapper;

import com.nooki.dto.ListingImage.ListingImageResponse;
import com.nooki.entity.ListingImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ListingImageMapper {
    @Mapping(target = "fileName", source = "fileName")
    ListingImageResponse toListingImageResponse(ListingImage listingImg);
}
