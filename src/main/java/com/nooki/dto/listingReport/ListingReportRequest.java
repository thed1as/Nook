package com.nooki.dto.listingReport;

import com.nooki.enums.listingReport.ListingReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ListingReportRequest {
    @NotNull
    private ListingReportReason reason;

    @Size(min = 10)
    private String description;
}
