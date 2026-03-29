package com.library.dto.location;

import lombok.Data;

@Data
public class LocationRequest {
    private String country;
    private String city;
    private String address;
}
