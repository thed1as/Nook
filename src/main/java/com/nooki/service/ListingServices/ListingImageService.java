package com.nooki.service.ListingServices;

import com.nooki.dto.exception.customException.ImageException.ImageStorageException;
import com.nooki.dto.listing.ListingResponse;
import com.nooki.entity.Listing;
import com.nooki.entity.ListingImage;
import com.nooki.mapper.ListingMapper;
import com.nooki.repository.ListingRepository;
import com.nooki.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingImageService {


    private final MinioService minioService;
    private final ListingDomainService domainService;
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    public void clearImages(Listing listing) {
        List<ListingImage> images = listing.getListingImages();
        try {
            for(ListingImage image : images) {
                minioService.deleteFile(image.getFileName());
            }
        } catch (Exception e) {
            log.error("Error deleting image failed", e);
            throw new ImageStorageException(e.getMessage());
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listings", key = "#listingId"),
            @CacheEvict(value = "filtered_listings", allEntries = true)
    })
    public ListingResponse addImageToListing(UUID listingId, List<MultipartFile> file) {
        Listing listing = domainService.getListingDetailedOrThrow(listingId);
        List<ListingImage> images = new ArrayList<>();

        try {
            for(MultipartFile fileItem : file) {
                String fileName = fileItem.getOriginalFilename();
                minioService.uploadFile(fileItem);


                ListingImage image = new ListingImage();
                image.setFileName(fileName);
                image.setListingImg(listing);

                images.add(image);
            }
            listing.addListingImage(images);

            listingRepository.save(listing);

        } catch (Exception e) {
            for(ListingImage image : images) {
                minioService.deleteFile(image.getFileName());
            }
            log.error("Error saving images for listing failed", e);
            throw new ImageStorageException("Failed to save listing, cleaning up files");
        }
        return listingMapper.toListingResponse(listing);
    }
}
