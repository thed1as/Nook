package com.nooki.dto.listing;

import com.nooki.dto.ListingImage.ListingImageResponse;
import com.nooki.dto.location.LocationResponse;
import com.nooki.dto.review.ReviewResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullListingResponse {
    private UUID listingId;
    private String listingTitle;
    private String listingDescription;
    @Builder.Default
    private List<ListingImageResponse> listingImages = new ArrayList<>();
    private LocationResponse location;
    private List<ReviewResponse> reviews;
    private Long reviewsCount;
    private Double averageRating;
    private BigDecimal pricePerNight;
    private String currency;
}
