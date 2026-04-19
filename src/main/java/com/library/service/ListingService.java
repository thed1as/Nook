package com.library.service;

import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.dto.listing.UpdateListingRequest;
import com.library.dto.location.LocationRequest;
import com.library.entity.Listing;
import com.library.entity.ListingImage;
import com.library.entity.Location;
import com.library.entity.User;
import com.library.mapper.ListingMapper;
import com.library.mapper.LocationMapper;
import com.library.repository.ListingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;
    private final UserService userService;
    private final ListingMapper listingMapper;
    private final LocationService locationService;
    private final MinioService minioService;
    private final LocationMapper locationMapper;

//    CREATE

    @Transactional
    public ListingResponse createListing(ListingRequest listingRequest,
                                         String email) {
        User user = userService.getUserByEmail(email);
        Listing listing = Listing.builder()
                .title(listingRequest.getListingTitle())
                .description(listingRequest.getDescription())
                .pricePerNight(listingRequest.getPricePerNight())
                .build();

        Location loc = locationService
                .createLocationOrGet(listingRequest.getLocationRequest());


        user.addListing(listing);
        loc.addListing(listing);

        listingRepository.save(listing);

        return listingMapper.toListingResponse(listing);
    }

    @Transactional
    public ListingResponse addImageToListing(UUID listingId, List<MultipartFile> file) {
        Listing listing = getListingOrThrow(listingId);
        List<ListingImage> images = new ArrayList<>();
        for(MultipartFile fileItem : file) {
            String url = minioService.uploadFile(fileItem);

            ListingImage image = new ListingImage();
            image.setUrl(url);
            image.setListingImg(listing);

            images.add(image);
        }
        listing.addListingImage(images);

        try {
            listingRepository.save(listing);
        } catch (DataAccessException e) {
            for(ListingImage image : images) {
                minioService.deleteFile(image.getUrl());
            }
            throw new RuntimeException("Failed to save listing, cleaning up files", e);
        }
        return listingMapper.toListingResponse(listing);
    }

//    UPDATE

    @Transactional
    public ListingResponse updateListing(UpdateListingRequest req,
                                         String email,
                                         UUID listingId) {
        Listing listing = getListingOrThrow(listingId);
        if(!listing.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("Not your listing");
        }

        LocationRequest newLoc = req.getLocationRequest();
        Location currentLoc = listing.getLocation();

        if (!currentLoc.getCountry().equals(newLoc.getCountry().toLowerCase()) ||
                !currentLoc.getCity().equals(newLoc.getCity().toLowerCase()) ||
                !currentLoc.getAddress().equals(newLoc.getAddress().toLowerCase())) {

            locationMapper.updateLocation(newLoc, listing.getLocation());
        }

        listingMapper.updateListing(req, listing);

        return listingMapper.toListingResponse(listing);
    }

//    SEARCHING

    @Transactional(readOnly = true)
    public ListingResponse getListingById(UUID listingId) {
        return listingRepository
                .findById(listingId)
                .map(listingMapper::toListingResponse)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getUsersListings(String email) {
        return listingRepository.findAllByUserEmail(email).stream()
                .map(listingMapper::toListingResponse).collect(Collectors.toList());
    }

    public Page<ListingResponse> getAll(Pageable pageable) {
        return listingRepository.findAll(pageable).map(listingMapper::toListingResponse);
    }

//  DELETE

    @Transactional
    public void deleteListingById(UUID listingId) {
        Listing listing = listingRepository.findById(listingId).orElseThrow(EntityNotFoundException::new);
        List<ListingImage> images = listing.getListingImages();
        for(ListingImage image : images) {
            minioService.deleteFile(image.getUrl());
        }
        listingRepository.delete(listing);
    }


//    Entity getters
    @Transactional(readOnly = true)
    public Listing getListingOrThrow(UUID listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("entity not exists"));
    }

}
