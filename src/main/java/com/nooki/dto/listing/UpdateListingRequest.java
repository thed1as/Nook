package com.nooki.dto.listing;

import com.nooki.dto.ListingImage.ListingImageRequest;
import com.nooki.dto.location.LocationRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateListingRequest {
    @Size(min = 3, max = 50, message = "title should be 3 to 50 symbols ")
    private String listingTitle;

    private String listingDescription;

    @Positive(message = "Price must be greater then zero")
    private BigDecimal pricePerNight;
    private String currency;
    private List<ListingImageRequest> listingImageRequest;
    private LocationRequest locationRequest;
}
