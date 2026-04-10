package com.library.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Booking request")
public class BookingRequest {

    @Schema(description = "Linsting ID")
    @NotBlank(message = "Listing ID is required")
    private UUID listingId;

    @NotBlank(message = "check in date required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    @Schema(example = "2026-04-10T14:00:00")
    private LocalDateTime checkInDate;

    @NotBlank(message = "check out date required")
    @FutureOrPresent(message = "Check-out date cannot be in the past")
    @Schema(example = "2026-04-10T14:00:00")
    private LocalDateTime checkOutDate;

}
