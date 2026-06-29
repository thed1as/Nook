package com.nooki.service.ListingServices;

import com.nooki.dto.PageResponse;
import com.nooki.dto.exception.customException.listingException.ListingIllegalStateException;
import com.nooki.dto.listing.ListingFilterRequest;
import com.nooki.dto.listing.ListingResponse;
import com.nooki.entity.Listing;
import com.nooki.entity.ListingSpecification;
import com.nooki.mapper.ListingMapper;
import com.nooki.repository.ListingRepository;
import com.nooki.service.CurrencyService;
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
