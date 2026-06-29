package com.nooki.service.ListingServices;

import com.nooki.dto.exception.customException.bookingException.BookingAccessDeniedException;
import com.nooki.dto.exception.customException.listingException.ListingCancelException;
import com.nooki.dto.exception.customException.listingException.ListingException;
import com.nooki.dto.exception.customException.listingException.ListingIllegalStateException;
import com.nooki.dto.exception.customException.listingException.ListingNotFoundException;
import com.nooki.dto.listing.ListingRequest;
import com.nooki.dto.listing.ListingResponse;
import com.nooki.dto.listing.UpdateListingRequest;
import com.nooki.dto.location.LocationRequest;
import com.nooki.entity.Listing;
import com.nooki.entity.Location;
import com.nooki.entity.User;
import com.nooki.mapper.ListingMapper;
import com.nooki.repository.BookingRepository;
import com.nooki.repository.ListingRepository;
import com.nooki.repository.UserRepository;
import com.nooki.service.LocationService;
import com.nooki.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingCommandService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;
    private final ListingDomainService domainService;
    private final BookingRepository bookingRepository;
    private final ListingImageService listingImageService;

    @Transactional
    @CacheEvict(value = "listings", allEntries = true)
    public ListingResponse createListing(ListingRequest listingRequest) {
        UUID userId = userService.getCurrentUserId();


        User user = userRepository.getReferenceById(userId);
        Listing listing = Listing.builder()
                .title(listingRequest.getListingTitle())
                .description(listingRequest.getDescription())
                .pricePerNight(listingRequest.getPricePerNight())
                .currency(listingRequest.getCurrency())
                .build();

        Location loc = locationService
                .createLocationOrGet(listingRequest.getLocationRequest());


        listing.setUser(user);
        loc.addListing(listing);

        listingRepository.save(listing);

        log.info("User {} created new listing with id {} successfully", listing.getListingId(), userId);

        return listingMapper.toListingResponse(listing);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listings", key = "#listingId")
    })
    public ListingResponse updateListing(UpdateListingRequest req,
                                         UUID listingId) {
        domainService.getListingOrThrow(listingId);
        UUID userId = userService.getCurrentUserId();
        Listing listing = domainService.getListingDetailedOrThrow(listingId);
        if(!listing.getUser().getUserId().equals(userId)) {
            log.warn("User: {} who don't own this listing trying to update listing: {}", userId, listingId);
            throw new ListingIllegalStateException("Not your listing");
        }

        if(req.getLocationRequest() != null) {
            LocationRequest newLoc = req.getLocationRequest();
            Location currentLoc = listing.getLocation();
            if (!currentLoc.getCountry().equals(newLoc.getCountry().toLowerCase()) ||
                    !currentLoc.getCity().equals(newLoc.getCity().toLowerCase()) ||
                    !currentLoc.getAddress().equals(newLoc.getAddress().toLowerCase())) {

                Location updLoc = locationService.createLocationOrGet(req.getLocationRequest());
                listing.setLocation(updLoc);
                log.debug("User: {} updated loc oldLoc: {}, newLoc: {}", userId, currentLoc.getLocationId(), updLoc.getLocationId());
            }
        }

        listingMapper.updateListing(req, listing);
        log.info("User {} updated listing {} successfully", userId, listingId);

        return listingMapper.toListingResponse(listing);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listings", key = "#listingId"),
    })
    public void deleteListingById(UUID listingId) {
        UUID userId = userService.getCurrentUserId();
        if(bookingRepository.existsActiveBookingsForListing(listingId)) {
            log.warn("User: {} tried to delete listing: {} with active future bookings", userId, listingId);
            throw new ListingCancelException("Cannot delete booking with active future bookings");
        }

        Listing listing = listingRepository.findById(listingId).orElseThrow(() -> new ListingNotFoundException("Listing not found"));
        if(!listing.getUser().getUserId().equals(userId)) {
            log.warn("User: {} tried to delete not his listing: {} ", userId, listingId);
            throw new ListingException("Not your listing");
        }
        listingImageService.clearImages(listing);

        listingRepository.deleteById(listing.getListingId());
        log.info("Listing {} and its image were permanently cleared by owner: {}", listingId, userId);
    }
}
