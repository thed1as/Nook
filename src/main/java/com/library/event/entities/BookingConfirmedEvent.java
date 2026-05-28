package com.library.event.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class BookingConfirmedEvent {
    private final UUID bookingId;
    private final String userEmail;
    private final String listingTitle;
    private final LocalDateTime checkIn;
    private final LocalDateTime checkOut;
    private final BigDecimal totalAmount;
}
