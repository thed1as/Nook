package com.nooki.dto.listingReport;

import com.nooki.enums.listingReport.ListingReportStatus;
import com.nooki.enums.listingReport.ListingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ListingPatchRequest {
    @NotNull
    private ListingStatus status;

    @NotBlank
    private String reviewedBy;

    @NotNull
    private ListingReportStatus listingReportStatus;
}
