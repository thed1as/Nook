package com.library.service.ListingServices;

import com.library.dto.PageResponse;
import com.library.dto.exception.customException.listingException.ListingIllegalStateException;
import com.library.dto.listing.*;
import com.library.entity.*;
import com.library.mapper.ListingMapper;
import com.library.repository.ListingRepository;
import com.library.repository.ReviewRepository;
import com.library.service.CurrencyService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingQueryService {
    private final ListingRepository listingRepository;
    private final ReviewRepository reviewRepository;
    private final CurrencyService currencyService;
    private final UserService userService;
    private final ListingMapper listingMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "listings", key = "#root.methodName + '_' + #listingId + '_' + #targetCurrency")
    public FullListingResponse getListingById(UUID listingId, String targetCurrency) {
        Listing listing = listingRepository.findByDetailedId(listingId)
                .orElseThrow(() -> new ListingIllegalStateException("No listing with id: " + listingId));

        BigDecimal convertedPrice = currencyService.convert(
                listing.getPricePerNight(), listing.getCurrency(), targetCurrency);

        List<Review> top3 = reviewRepository
                .findTop3ByListing_ListingIdOrderByCreatedAtDesc(listingId);

        FullListingResponse response = listingMapper.toFullListingResponse(listing, top3);
        response.setPricePerNight(convertedPrice);
        response.setCurrency(targetCurrency);

        return response;
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> getUsersListings(Pageable pageable, String currency) {
        UUID userId = userService.getCurrentUserId();
        Page<UUID> ids = listingRepository.findAllIdsByUserId(userId, pageable);

        List<Listing> listings = listingRepository.findAllDetailedByUserEmail(ids.getContent());

        List<ListingResponse> processedResponses = listings.stream()
                .map(listing -> {
                    BigDecimal convertedPrice = currencyService.convert(listing.getPricePerNight(), listing.getCurrency(), currency);
                    ListingResponse listingResponse = listingMapper.toListingResponse(listing);

                    listingResponse.setPricePerNight(convertedPrice);
                    listingResponse.setCurrency(listing.getCurrency());

                    return listingResponse;
                })
                .toList();

        return new PageImpl<>(processedResponses, pageable, ids.getTotalElements());
    }

    @Cacheable(value = "listings", key = "#root.methodName + '_' + #pageable")
    public PageResponse<ShortListingResponse> getAll(Pageable pageable) {
        Page<UUID> idPage = listingRepository.findAllIds(pageable);

        List<Listing> listings = listingRepository.findAllByDetailedIds(idPage.getContent());
        List<ShortListingResponse> responses = listings.stream().map(listingMapper::toShortListingResponse).toList();

        Page<ShortListingResponse> responsePage = new PageImpl<>(responses, pageable, idPage.getTotalElements());
        return PageResponse.from(responsePage);
    }
}
