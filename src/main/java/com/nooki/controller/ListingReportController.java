package com.nooki.controller;

import com.nooki.dto.listingReport.FullListingReportResponse;
import com.nooki.dto.listingReport.ListingPatchRequest;
import com.nooki.dto.listingReport.ListingReportRequest;
import com.nooki.dto.listingReport.ListingReportResponse;
import com.nooki.service.ListingServices.ListingReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ListingReportController {
    private final ListingReportService listingReportService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/listings/{listingId}/reports")
    public ResponseEntity<ListingReportResponse> create(@PathVariable UUID listingId, @Valid @RequestBody ListingReportRequest listingReportRequest) {
        ListingReportResponse resp = listingReportService.createReport(listingId, listingReportRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/reports/me")
    public ResponseEntity<Page<ListingReportResponse>> getMyReports(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<ListingReportResponse> resp = listingReportService.myReports(pageable);
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports")
    public ResponseEntity<Page<FullListingReportResponse>> getOpenReports(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<FullListingReportResponse> resp = listingReportService.getUnreviewedReports(pageable);
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/reports/{reportId}/resolve")
    public ResponseEntity<FullListingReportResponse> patchReport(@PathVariable Long reportId,
                                                                       @Valid @RequestBody ListingPatchRequest request) {
        FullListingReportResponse resp = listingReportService.patchReport(reportId, request);
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<FullListingReportResponse> getReport(@PathVariable Long reportId) {
        FullListingReportResponse resp = listingReportService.getReport(reportId);
        return ResponseEntity.ok(resp);
    }

}
