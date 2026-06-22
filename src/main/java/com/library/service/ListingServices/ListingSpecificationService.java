package com.library.service.ListingServices;

import com.library.dto.PageResponse;
import com.library.dto.exception.customException.listingException.ListingIllegalStateException;
import com.library.dto.listing.ListingFilterRequest;
import com.library.dto.listing.ListingResponse;
import com.library.entity.Listing;
import com.library.entity.ListingSpecification;
import com.library.mapper.ListingMapper;
import com.library.repository.ListingRepository;
import com.library.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListingSpecificationService {

    private final ListingRepository listingRepository;
    private final CurrencyService currencyService;
    private final ListingMapper listingMapper;

    @Cacheable(value = "filtered_listings", keyGenerator = "filterCacheKeyGenerator")
    public PageResponse<ListingResponse> getListingsByFilter(String currency,
                                                             ListingFilterRequest listingFilterRequest,
                                                             Pageable pageable) {
        if(listingFilterRequest.getCheckOut() != null && listingFilterRequest.getCheckIn() != null && !listingFilterRequest.getCheckOut().isAfter(listingFilterRequest.getCheckIn())) {
            log.warn("Validation failed from: {}, to: {}", listingFilterRequest.getCheckIn(), listingFilterRequest.getCheckOut());
            throw new ListingIllegalStateException("Invalid date range");
        }

        Specification<Listing> spec = ListingSpecification.build(listingFilterRequest);
        Page<Listing> res = listingRepository.findAll(spec, pageable);

        List<ListingResponse> resp = res.stream().map(
                listing -> {
                    BigDecimal convertedPrice = currencyService.convert(listing.getPricePerNight(), listing.getCurrency(), currency);
                    ListingResponse listingResponse = listingMapper.toListingResponse(listing);

                    listingResponse.setPricePerNight(convertedPrice);
                    listingResponse.setCurrency(currency);

                    return listingResponse;
                }
        ).toList();

        Page<ListingResponse> pageResp = new PageImpl<>(resp, pageable, res.getTotalElements());

        return PageResponse.from(pageResp);
    }
}
