package com.library.dto.booking;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingRequest {
    @NotBlank(message = "Listing ID is required")
    private UUID listingId;

    @NotBlank(message = "check in date required")
    private LocalDateTime checkInDate;

    @NotBlank(message = "check out date required")
    private LocalDateTime checkOutDate;

}
