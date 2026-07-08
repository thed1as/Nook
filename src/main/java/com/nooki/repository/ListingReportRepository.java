package com.nooki.repository;

import com.nooki.entity.ListingReport;
import com.nooki.enums.listingReport.ListingReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ListingReportRepository extends JpaRepository<ListingReport, Long> {
    Page<ListingReport> findListingReportByUser_UserId(UUID userId, Pageable pageable);

    Page<ListingReport> findListingReportByStatus(ListingReportStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"listing"})
    Optional<ListingReport> findListingReportByReportId(Long reportId);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE ListingReport lr
        SET lr.status = :status,
            lr.reviewedBy = :reviewedBy,
            lr.resolvedAt = :resolvedAt,
            lr.version = lr.version + 1
        WHERE lr.listing.listingId = :listingId
            AND lr.status = :oldStatus
        """)
    void resolveAllReports(
            UUID listingId,
            ListingReportStatus oldStatus,
            ListingReportStatus status,
            String reviewedBy,
            LocalDateTime resolvedAt
    );
}
