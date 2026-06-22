package com.library.dto.listing;

import com.library.dto.ListingImage.ListingImageResponse;
import com.library.dto.location.LocationResponse;
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
