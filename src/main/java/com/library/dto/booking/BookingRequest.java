package com.library.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingRequest {
    @NotNull(message = "Listing ID is required")
    private UUID listingId;

    @NotNull(message = "check in date required")
    private LocalDateTime checkInDate;

    @NotNull(message = "check out date required")
    private LocalDateTime checkOutDate;

}
