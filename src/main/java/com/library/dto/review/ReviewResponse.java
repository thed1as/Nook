package com.library.dto.review;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID reviewId;
    private BigDecimal rating;
    private String comment;
    private UUID username;
    private UUID listingId;
}
