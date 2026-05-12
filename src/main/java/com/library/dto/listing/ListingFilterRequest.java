package com.library.dto.listing;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ListingFilterRequest {

    @PositiveOrZero(message = "minimum price cannot be negative")
    private BigDecimal minPrice;

    @Positive
    private BigDecimal maxPrice;

    private String country;
    private String city;

    @FutureOrPresent(message = "Check-in date cannot be in past")
    private LocalDateTime checkIn;

    @FutureOrPresent(message = "Check-in date cannot be in past")
    private LocalDateTime checkOut;
}
