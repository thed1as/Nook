package com.library.dto.booking;

import com.library.entity.ListingImage;
import com.library.entity.Location;
import com.library.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {

    private UUID id;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private BigDecimal totalPrice;
    private Status status;

    private UUID listingId;
    private String listingTitle;
    private String listingDescription;
    private List<ListingImage> listingImage;
    private Location location;

    private UUID userId;
    private String fullName;

}
