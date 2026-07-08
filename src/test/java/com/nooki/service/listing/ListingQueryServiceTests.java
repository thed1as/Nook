package com.nooki.service.listing;

import com.nooki.dto.PageResponse;
import com.nooki.dto.exception.customException.listingException.ListingIllegalStateException;
import com.nooki.dto.listing.*;
import com.nooki.dto.review.ReviewResponse;
import com.nooki.entity.*;
import com.nooki.mapper.ListingMapper;
import com.nooki.repository.ListingRepository;
import com.nooki.repository.ReviewRepository;
import com.nooki.service.CurrencyService;
import com.nooki.service.ListingServices.ListingQueryService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.nooki.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingQueryServiceTests {

    @InjectMocks
    private ListingQueryService listingQueryService;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private UserService userService;

    @Mock
    private ListingMapper listingMapper;

    @Nested
    @DisplayName("getListingById")
    class getListing {

        @Test
        @DisplayName("get listing by id exists listing should return FullListingResponse")
        void validRequest_shouldReturnFullListingResponse() {
            UUID listingId = UUID.randomUUID();
            String targetCurrency = "EUR";

            Listing listing = new Listing();
            listing.setListingId(listingId);

            listing.setPricePerNight(new BigDecimal("20"));
            listing.setCurrency("USD");

            Review review1 = new Review();
            Review review2 = new Review();
            Review review3 = new Review();

            review1.setListing(listing);
            review2.setListing(listing);
            review3.setListing(listing);

            List<Review> list = List.of(review1, review2, review3);
            List<ReviewResponse> listResponse = List.of(
                    new ReviewResponse(),
                    new ReviewResponse(),
                    new ReviewResponse());

            FullListingResponse expectedResponse = new FullListingResponse();
            expectedResponse.setListingId(listingId);
            expectedResponse.setPricePerNight(new BigDecimal("17.55"));
            expectedResponse.setCurrency("EUR");
            expectedResponse.setReviews(listResponse);

            when(listingRepository.findByDetailedId(listingId))
                    .thenReturn(Optional.of(listing));
            when(currencyService.convert(listing.getPricePerNight(), listing.getCurrency(), targetCurrency))
                    .thenReturn(new BigDecimal("17.55"));
            when(reviewRepository.findTop3ByListing_ListingIdOrderByCreatedAtDesc(listingId))
                    .thenReturn(list);
            when(listingMapper.toFullListingResponse(listing, list))
                    .thenReturn(expectedResponse);

            FullListingResponse result = listingQueryService.getPublicListingById(listingId, targetCurrency);

            verify(listingRepository, times(1)).findByDetailedId(listingId);

            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.getReviews()).isEqualTo(listResponse);
            assertThat(result.getCurrency()).isEqualTo(expectedResponse.getCurrency());
            assertThat(result.getPricePerNight()).isEqualTo(new BigDecimal("17.55"));
        }

        @Test
        @DisplayName("get listing by id listing not found should throw ListingIllegalStateException")
        void listingNotFound_shouldThrowListingIllegalStateException() {
            UUID listingId = UUID.randomUUID();
            String targetCurr = "USD";
            assertThatThrownBy(() -> listingQueryService.getPublicListingById(listingId, targetCurr))
                    .isInstanceOf(ListingIllegalStateException.class)
                    .hasMessage("No listing with id: " + listingId);
        }

        @Test
        @DisplayName("get users listing valid request should return page of ListingResponse")
        void validRequest_shouldReturnPageOfListingResponse() {
            UUID userId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            String currency = "USD";

            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID id3 = UUID.randomUUID();

            List<UUID> ids = List.of(id1, id2, id3);
            Page<UUID> idsPage = new PageImpl<>(ids, pageable, ids.size());

            Listing l1 = new Listing(); l1.setPricePerNight(new BigDecimal("17.55")); l1.setCurrency("EUR");
            Listing l2 = new Listing(); l2.setPricePerNight(new BigDecimal("17.55")); l1.setCurrency("EUR");
            Listing l3 = new Listing(); l3.setPricePerNight(new BigDecimal("17.55")); l1.setCurrency("EUR");

            l1.setListingId(id1); l2.setListingId(id2); l3.setListingId(id3);

            List<Listing> listings = List.of(l1, l2, l3);

            ListingResponse lr1 = new ListingResponse();
            ListingResponse lr2 = new ListingResponse();
            ListingResponse lr3 = new ListingResponse();

            lr1.setListingId(id1); lr2.setListingId(id2); lr3.setListingId(id3);

            List<ListingResponse> listingResponse = List.of(lr1, lr2, lr3);

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(listingRepository.findAllIdsByUserId(userId, pageable))
                    .thenReturn(idsPage);
            when(listingRepository.findAllDetailedByUserId(ids)).thenReturn(listings);
            when(currencyService.convert(any(), any(), any())).thenReturn(new BigDecimal("20.00"));
            when(listingMapper.toListingResponse(any(Listing.class))).thenReturn(lr1, lr2, lr3);

            Page<ListingResponse> result = listingQueryService.getUsersListings(pageable, currency);

            assertThat(result).isNotNull();
            assertThat(result.getContent().size()).isEqualTo(3);
            assertThat(result.getContent().getFirst().getListingId()).isEqualTo(id1);

            verify(listingRepository, times(1)).findAllDetailedByUserId(idsPage.getContent());
        }

        @Test
        @DisplayName("get all listing valid request should return PageResponse of ShortListingResponse")
        void validRequest_shouldReturnPageResponseOfShortListingResponse() {
            Pageable pageable = PageRequest.of(0, 10);
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID id3 = UUID.randomUUID();

            Page<UUID> idPage = new PageImpl<>(List.of(id1,id2,id3), pageable, 3);

            Listing l1 = new Listing(); l1.setListingId(id1);
            Listing l2 = new Listing(); l2.setListingId(id2);
            Listing l3 = new Listing(); l3.setListingId(id3);

            List<Listing> listings = List.of(l1,l2,l3);

            ShortListingResponse lr1 = new ShortListingResponse(); lr1.setListingId(id1);
            ShortListingResponse lr2 = new ShortListingResponse(); lr2.setListingId(id1);
            ShortListingResponse lr3 = new ShortListingResponse(); lr3.setListingId(id1);
            List<ShortListingResponse> listingResponse = List.of(lr1, lr2, lr3);


            when(listingRepository.findAllIds(pageable))
                    .thenReturn(idPage);
            when(listingRepository.findAllByDetailedIds(idPage.getContent()))
                    .thenReturn(listings);
            when(listingMapper.toShortListingResponse(l1)).thenReturn(lr1);
            when(listingMapper.toShortListingResponse(l2)).thenReturn(lr2);
            when(listingMapper.toShortListingResponse(l3)).thenReturn(lr3);

            PageResponse<ShortListingResponse> result = listingQueryService.getAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.content().size()).isEqualTo(3);
            assertThat(result.content().getFirst().getListingId()).isEqualTo(id1);
        }
    }
}
