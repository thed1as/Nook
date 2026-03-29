package com.library.dto.listing;

import com.library.entity.ListingImage;
import com.library.entity.Location;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ListingRequest {
    @NotBlank(message = "listing title is required")
    private String listingTitle;
    private String description;
    @NotBlank(message = "listing price per night is required")
    private BigDecimal pricePerNight;
    @NotBlank(message = "listing image is required")
    private ListingImage listingImage;
    @NotBlank(message = "listing location is required")
    private Location location;
}