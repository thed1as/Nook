package com.nooki.event.entities;

import java.util.UUID;

public record ListingSuspendedEvent (UUID listingId, String initiatedBy) {
}
