package com.library.dto.booking;

import com.library.dto.ListingImage.ListingImageResponse;
import com.library.dto.location.LocationResponse;
import com.library.entity.ListingImage;
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

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private BigDecimal totalPrice;
    private Status status;

    private UUID listingId;
    private String listingTitle;
    private String listingDescription;
    private List<ListingImageResponse> listingImage;
    private LocationResponse location;

    private String username;

}