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
public class FullListingReportResponse {
    private Long reportId;
    private UUID listingId;
    private ListingReportReason reason;
    private String description;
    private ListingReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String reviewedBy;
}
