package com.library.dto.ListingImage;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@Builder
public class ListingImageResponse {
    private UUID id;
    private String url;
}
