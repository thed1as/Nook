package com.library.service;

import com.library.dto.listing.*;
import com.library.dto.location.LocationRequest;
import com.library.entity.*;
import com.library.mapper.ListingMapper;
import com.library.mapper.ReviewMapper;
import com.library.repository.BookingRepository;
import com.library.repository.ListingRepository;
import com.library.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;
    private final LocationService locationService;
    private final MinioService minioService;
    private final ListingMapper listingMapper;

//    CREATE

    @Transactional
    public ListingResponse createListing(ListingRequest listingRequest) {
        User user = userService.getUserByEmail(userService.getCurrentUserEmail());
        Listing listing = Listing.builder()
                .title(listingRequest.getListingTitle())
                .description(listingRequest.getDescription())
                .pricePerNight(listingRequest.getPricePerNight())
                .build();

        Location loc = locationService
                .createLocationOrGet(listingRequest.getLocationRequest());


        listing.setUser(user);
        loc.addListing(listing);

        listingRepository.save(listing);

        return listingMapper.toListingResponse(listing);
    }

    @Transactional
    public ListingResponse addImageToListing(UUID listingId, List<MultipartFile> file) {
        Listing listing = getListingOrThrow(listingId);
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
            throw new RuntimeException("Failed to save listing, cleaning up files", e);
        }
        return listingMapper.toListingResponse(listing);
    }

//    UPDATE

    @Transactional
    public ListingResponse updateListing(UpdateListingRequest req,
                                         UUID listingId) {
        getListingWithLockOrThrow(listingId);
        String email = userService.getCurrentUserEmail();
        Listing listing = getListingDetailedOrThrow(listingId);
        if(!listing.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("Not your listing");
        }

        if(req.getLocationRequest() != null) {
            LocationRequest newLoc = req.getLocationRequest();
            Location currentLoc = listing.getLocation();
            if (!currentLoc.getCountry().equals(newLoc.getCountry().toLowerCase()) ||
                    !currentLoc.getCity().equals(newLoc.getCity().toLowerCase()) ||
                    !currentLoc.getAddress().equals(newLoc.getAddress().toLowerCase())) {

                Location updLoc = locationService.createLocationOrGet(req.getLocationRequest());
                listing.setLocation(updLoc);
            }
        }

        listingMapper.updateListing(req, listing);

        return listingMapper.toListingResponse(listing);
    }

//    SEARCHING

        @Transactional(readOnly = true)
        public FullListingResponse getListingById(UUID listingId) {
            Listing listing = listingRepository.findByDetailedId(listingId)
                    .orElseThrow(EntityNotFoundException::new);

            List<Review> top3 = reviewRepository
                    .findTop3ByListing_ListingIdOrderByCreatedAtDesc(listingId);

            return listingMapper.toFullListingResponse(listing, top3);
        }

    @Transactional(readOnly = true)
    public Page<ListingResponse> getUsersListings(String email, Pageable pageable) {
        Page<UUID> ids = listingRepository.findAllIdsByUserEmail(email, pageable);

        if(ids.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Listing> listings = listingRepository.findAllDetailedByUserEmail(ids.getContent());

        return new PageImpl<>(listings, pageable, ids.getTotalElements()).map(listingMapper::toListingResponse);
    }

    public Page<ShortListingResponse> getAll(Pageable pageable) {
        Page<UUID> idPage = listingRepository.findAllIds(pageable);

        if(idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Listing> listings = listingRepository.findAllByDetailedIds(idPage.getContent());

        return new PageImpl<>(listings, pageable, idPage.getTotalElements()).map(listingMapper::toShortListingResponse);
    }

    public Page<ListingResponse> getListingsByFilter(ListingFilterRequest listingFilterRequest, Pageable pageable) {
        if(listingFilterRequest.getCheckOut() != null && listingFilterRequest.getCheckIn() != null && !listingFilterRequest.getCheckOut().isAfter(listingFilterRequest.getCheckIn())) {
            throw new IllegalStateException("Invalid date range");
        }

        Specification<Listing> spec = ListingSpecification.build(listingFilterRequest);
        Page<Listing> res = listingRepository.findAll(spec, pageable);

        if (res.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> listingIds = res.getContent().stream().map(Listing::getListingId).toList();

        return res.map(listingMapper::toListingResponse);
    }

//  DELETE

    @Transactional
    public void deleteListingById(UUID listingId) {
        if(bookingRepository.existsById(listingId)) {
            throw new IllegalStateException("Booking doesn't exists");
        }
        if(bookingRepository.existsActiveBookingsForListing(listingId)) {
            throw new IllegalStateException("Cannot delete booking with active future bookings");
        }

        String email = userService.getCurrentUserEmail();
        Listing listing = listingRepository.findById(listingId).orElseThrow(EntityNotFoundException::new);
        if(!listing.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("Not your listing");
        }
        List<ListingImage> images = listing.getListingImages();
        for(ListingImage image : images) {
            minioService.deleteFile(image.getFileName());
        }
        listingRepository.deleteDetailedById(listing.getListingId());
    }


//    Entity getters
    @Transactional(readOnly = true)
    public Listing getListingOrThrow(UUID listingId) {
        return listingRepository.findByIdWithLock(listingId)
                .orElseThrow(() -> new EntityNotFoundException("entity not exists"));
    }

    @Transactional(readOnly = true)
    public void getListingWithLockOrThrow(UUID listingId) {
        listingRepository.findByIdWithLock(listingId)
                .orElseThrow(() -> new EntityNotFoundException("entity not exists"));
    }
    @Transactional(readOnly = true)
    public Listing getListingDetailedOrThrow(UUID listingId) {
        return listingRepository.findByDetailedId(listingId)
                .orElseThrow(() -> new EntityNotFoundException("entity not exists"));
    }
}
