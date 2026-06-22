package com.library.dto.listing;

import com.library.dto.ListingImage.ListingImageResponse;
import com.library.dto.location.LocationResponse;
import com.library.dto.review.ReviewResponse;
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
public class ListingResponse {
    private UUID listingId;
    private String listingTitle;
    private String listingDescription;
    @Builder.Default
    private List<ListingImageResponse> listingImages = new ArrayList<>();
    private LocationResponse location;
    private BigDecimal pricePerNight;
    private String currency;
}
