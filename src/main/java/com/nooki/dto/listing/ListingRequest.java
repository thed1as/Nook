package com.nooki.dto.listing;

import com.nooki.dto.location.LocationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ListingRequest {
    @Schema(description = "Listing title")
    @NotBlank(message = "listing title is required")
    @Size(min = 3, max = 50, message = "title should be 3 to 50 symbols ")
    private String listingTitle;

    @Schema(description = "Listing description")
    private String description;

    @Schema(description = "Listing price per night")
    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be greater then zero")
    private BigDecimal pricePerNight;

    @NotBlank(message = "currency cannot be null")
    @Size(max = 3)
    private String currency;

    @Schema(description = "Listing Location")
    @NotNull(message = "listing location is required")
    private LocationRequest locationRequest;
}