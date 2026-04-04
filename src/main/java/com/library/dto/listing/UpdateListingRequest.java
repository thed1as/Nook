package com.library.dto.listing;

import com.library.dto.ListingImage.ListingImageRequest;
import com.library.dto.location.LocationRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateListingRequest {
    private String listingTitle;
    private String listingDescription;
    private BigDecimal pricePerNight;
    private List<ListingImageRequest> listingImageRequest;
    private LocationRequest locationRequest;
}
