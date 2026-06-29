package com.nooki.dto.listing;

import com.nooki.dto.ListingImage.ListingImageResponse;
import com.nooki.dto.location.LocationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortListingResponse {
    private ListingImageResponse previewImage;
    private String listingTitle;
    private UUID listingId;
    private LocationResponse location;
    private Double averageRating;
    private Long reviewsCount;
}
