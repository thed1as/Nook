package com.library.dto.listing;

import com.library.dto.ListingImage.ListingImageRequest;
import com.library.dto.location.LocationRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ListingRequest {
    @NotBlank(message = "listing title is required")
    private String listingTitle;
    private String description;
    @NotBlank(message = "listing price per night is required")
    private BigDecimal pricePerNight;
    @NotBlank(message = "listing image is required")
    private List<ListingImageRequest> listingImageRequests;
    @NotBlank(message = "listing location is required")
    private LocationRequest locationRequest;
}