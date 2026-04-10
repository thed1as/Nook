package com.library.dto.location;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationRequest {
    @NotBlank(message = "country can't be empty")
    private String country;
    @NotBlank(message = "country can't be empty")
    private String city;
    @NotBlank(message = "country can't be empty")
    private String address;
}
