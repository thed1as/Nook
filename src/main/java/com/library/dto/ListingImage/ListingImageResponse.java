package com.library.dto.ListingImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
public class ListingImageResponse {
    private Long id;
    private String url;
}
