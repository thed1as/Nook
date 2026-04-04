package com.library.service;

import com.library.dto.ListingImage.ListingImageRequest;
import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.dto.listing.UpdateListingRequest;
import com.library.entity.Listing;
import com.library.entity.ListingImage;
import com.library.entity.Location;
import com.library.entity.User;
import com.library.mapper.ListingMapper;
import com.library.repository.ListingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;
    private final UserService userService;
    private final ListingMapper listingMapper;
    private final LocationService locationService;
    private final MinioService minioService;

    @Transactional
    public ListingResponse createListing(@Valid ListingRequest listingRequest,
                                         List<MultipartFile> files,
                                         UUID userId) {
        User user = userService.getUserOrThrow(userId);

        Listing listing = Listing.builder()
                .title(listingRequest.getListingTitle())
                .description(listingRequest.getDescription())
                .pricePerNight(listingRequest.getPricePerNight())
                .build();

        Location loc = locationService
                .createLocationOrGet(listingRequest.getLocationRequest());

        List<ListingImage> images = new ArrayList<>();

        for(MultipartFile file : files) {
            String url = minioService.uploadFile(file);

            ListingImage image = new ListingImage();
            image.setUrl(url);
            image.setListingImg(listing);

            images.add(image);
        }

        listing.addListingImage(images);
        user.addListing(listing);
        loc.addListing(listing);

        listingRepository.save(listing);

        return listingMapper.toListingResponse(listing);
    }

    @Transactional
    public ListingResponse updateListing(UpdateListingRequest req,
                                         UUID userId,
                                         UUID listingId) {
        Listing listing = getListingOrThrow(listingId);
        if(!listing.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("Not your listing");
        }

        if(req.getLocationRequest() != null) {
            listing.setLocation(locationService
                    .createLocationOrGet(req.getLocationRequest())
            );
        }

        listingMapper.updateListing(req, listing);

        return listingMapper.toListingResponse(listing);
    }

    @Transactional(readOnly = true)
    public ListingResponse getListingById(UUID listingId) {
        return listingRepository
                .findById(listingId)
                .map(listingMapper::toListingResponse)
                .orElseThrow(EntityNotFoundException::new);
    }

//    set role admin
    @Transactional
    public void deleteListingById(UUID listingId) {
        listingRepository.deleteById(listingId);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getUsersListings(UUID userId) {
        return listingRepository.findAllByUserId(userId);
    }

//    Entity getters
    @Transactional(readOnly = true)
    public Listing getListingOrThrow(UUID listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("entity not exists"));
    }
}
