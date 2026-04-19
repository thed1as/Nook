package com.library.dto.listing;

import com.library.dto.ListingImage.ListingImageResponse;
import com.library.dto.location.LocationResponse;
import com.library.dto.review.ReviewResponse;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
@Builder
public class ListingResponse {
    private String listingTitle;
    private String listingDescription;
    private List<ListingImageResponse> listingImage = new ArrayList<>();
    private LocationResponse location;
    private List<ReviewResponse> reviews;
    private int pricePerNight;
}
