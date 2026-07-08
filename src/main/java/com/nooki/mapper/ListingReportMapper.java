package com.nooki.mapper;

import com.nooki.dto.listingReport.FullListingReportResponse;
import com.nooki.dto.listingReport.ListingReportResponse;
import com.nooki.entity.ListingReport;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ListingReportMapper {
    @Mapping(target = "listingId", source = "listing.listingId")
    ListingReportResponse toListingReportResponse(ListingReport listingReport);

    @Mapping(target = "listingId", source = "listing.listingId")
    FullListingReportResponse toFullListingReportResponse(ListingReport listingReport);
}
