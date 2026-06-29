package com.nooki.dto.ListingImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ListingImageResponse {
    private Long id;
    private String url;
}
