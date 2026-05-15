package com.library.dto.review;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateReviewRequest {
    @Positive
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    @NotNull
    private BigDecimal rating;
    private String comment;
}
