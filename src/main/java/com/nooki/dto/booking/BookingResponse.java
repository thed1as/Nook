package com.nooki.dto.booking;

import com.nooki.dto.ListingImage.ListingImageResponse;
import com.nooki.dto.location.LocationResponse;
import com.nooki.dto.review.ReviewResponse;
import com.nooki.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private BigDecimal totalPrice;
    private String currency;
    private Status status;

    private UUID listingId;
    private String listingTitle;
    private String listingDescription;
    private List<ListingImageResponse> listingImage;
    private LocationResponse location;

    private String username;

}