package com.library.event.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PaymentCompletedEvent {
    private final UUID paymentId;
    private final String userEmail;
    private final String listingTitle;
    private final BigDecimal amount;
    private final String currency;
}
