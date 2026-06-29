package com.nooki.event.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class BookingCancelEvent {
    private final UUID bookingId;
    private final String userEmail;
    private final String listingTitle;
}
