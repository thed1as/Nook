package com.library.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Booking request")
public class BookingRequest {

    @Schema(description = "Linsting ID")
    @NotNull(message = "Listing ID is required")
    private UUID listingId;

    @NotNull(message = "check in date required")
    @Schema(example = "2026-04-10T14:00:00")
    private LocalDateTime checkInDate;

    @NotNull(message = "check out date required")
    private LocalDateTime checkOutDate;

}
