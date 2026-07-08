package com.nooki.dto.listingReport;

import com.nooki.enums.listingReport.ListingReportReason;
import com.nooki.enums.listingReport.ListingReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingReportResponse {
    private ListingReportStatus status;
    private UUID listingId;
    private ListingReportReason reason;
    private String reviewedBy;
    private LocalDateTime resolvedAt;
}
