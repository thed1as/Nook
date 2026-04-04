package com.library.dto.ListingImage;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ListingImageRequest {
    private String altText;
    private int order;
}
