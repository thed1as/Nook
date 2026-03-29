package com.library.dto.review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ReviewRequest {
    @NotBlank(message = "Rating is required")
    private BigDecimal rating;
    private String comment;

    @NotBlank(message = "Username is required")
    private UUID username;
    @NotBlank(message = "Listing id required")
    private UUID listingTitle;
}
