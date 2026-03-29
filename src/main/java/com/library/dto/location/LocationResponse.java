package com.library.dto.location;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LocationResponse {
    private UUID locationId;
    private String country;
    private String city;
    private String address;
}
