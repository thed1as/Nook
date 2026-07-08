package com.nooki.service.ListingServices;

import com.nooki.dto.exception.customException.listingReportException.ListingReportException;
import com.nooki.dto.exception.customException.listingException.ListingNotFoundException;
import com.nooki.dto.exception.customException.listingReportException.ListingReportNotFoundException;
import com.nooki.dto.exception.customException.userException.UserNotFoundException;
import com.nooki.dto.listingReport.FullListingReportResponse;
import com.nooki.dto.listingReport.ListingPatchRequest;
import com.nooki.dto.listingReport.ListingReportRequest;
import com.nooki.dto.listingReport.ListingReportResponse;
import com.nooki.entity.Listing;
import com.nooki.entity.ListingReport;
import com.nooki.entity.User;
import com.nooki.enums.listingReport.ListingReportStatus;
import com.nooki.enums.listingReport.ListingStatus;
import com.nooki.event.entities.ListingSuspendedEvent;
import com.nooki.mapper.ListingReportMapper;
import com.nooki.repository.ListingReportRepository;
import com.nooki.repository.ListingRepository;
import com.nooki.repository.UserRepository;
import com.nooki.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingReportService {
    private final ListingReportRepository listingReportRepository;
    private final ListingRepository listingRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ListingReportMapper listingReportMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ListingReportResponse createReport(UUID listingId, ListingReportRequest listingReportRequest) {
        Listing listing = listingRepository.findByDetailedId(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing with id " + listingId + " not found"));
        UUID userId = userService.getCurrentUserId();
        if(userId.equals(listing.getUser().getUserId())) {
            log.warn("User {} tried to report his own listing {}", userId, listingId);
            throw new ListingReportException("You can't report your listing");
        }
        User user = userRepository.getReferenceById(userId);

        ListingReport listingReport = ListingReport.builder()
                .reason(listingReportRequest.getReason())
                .description(listingReportRequest.getDescription())
                .status(ListingReportStatus.OPEN)
                .listing(listing)
                .user(user)
                .build();

        return listingReportMapper.toListingReportResponse(listingReportRepository.save(listingReport));
    }

    @Transactional
    public Page<ListingReportResponse> myReports(Pageable pageable) {
        UUID userId = userService.getCurrentUserId();
        return listingReportRepository.findListingReportByUser_UserId(userId, pageable)
                .map(listingReportMapper::toListingReportResponse);
    }

    @Transactional
    public Page<FullListingReportResponse> getUnreviewedReports(Pageable pageable) {
        return listingReportRepository.findListingReportByStatus(ListingReportStatus.OPEN, pageable)
                .map(listingReportMapper::toFullListingReportResponse);
    }

    @Transactional
    public FullListingReportResponse patchReport(Long reportId, ListingPatchRequest listingPatchRequest) {
        ListingReport report = listingReportRepository.findListingReportByReportId(reportId)
                .orElseThrow(() -> new ListingReportNotFoundException("Report with id " + reportId + " not found"));

        UUID userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with this id not exists"));
        if(report.getStatus() != ListingReportStatus.OPEN) {
            log.warn("Admin {} tried to resolve already resolved report", userId);
            throw new ListingReportException("This report is already resolved");
        }

        Listing l = report.getListing();
        if(l.getListingStatus().equals(ListingStatus.SUSPENDED)) {
            throw new ListingReportException("This report is already suspended");
        }

        String reviewedBy = user.getUsername() + "." + '\n' + listingPatchRequest.getReviewedBy();

        report.setStatus(listingPatchRequest.getListingReportStatus());
        report.setReviewedBy(reviewedBy);
        report.setResolvedAt(LocalDateTime.now());

        l.setListingStatus(listingPatchRequest.getStatus());
        listingRepository.save(l);

        if(listingPatchRequest.getStatus() == ListingStatus.SUSPENDED) {
            listingReportRepository.resolveAllReports(
                    report.getListing().getListingId(),
                    ListingReportStatus.OPEN,
                    ListingReportStatus.AUTO_RESOLVED,
                    reviewedBy,
                    LocalDateTime.now()
            );

            eventPublisher.publishEvent(new ListingSuspendedEvent(l.getListingId(), reviewedBy));
        }

        return listingReportMapper.toFullListingReportResponse(listingReportRepository.save(report));
    }

    public FullListingReportResponse getReport(Long reportId) {
        return listingReportRepository.findListingReportByReportId(reportId).map(listingReportMapper::toFullListingReportResponse)
                .orElseThrow(() -> new EntityNotFoundException("Report with id " + reportId + " not found"));
    }
}
