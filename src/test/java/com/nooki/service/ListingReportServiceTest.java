package com.nooki.service;

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
import com.nooki.enums.listingReport.ListingReportReason;
import com.nooki.enums.listingReport.ListingReportStatus;
import com.nooki.enums.listingReport.ListingStatus;
import com.nooki.event.entities.ListingSuspendedEvent;
import com.nooki.mapper.ListingReportMapper;
import com.nooki.repository.ListingReportRepository;
import com.nooki.repository.ListingRepository;
import com.nooki.repository.UserRepository;
import com.nooki.service.ListingServices.ListingReportService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingReportServiceTest {

    @InjectMocks
    private ListingReportService listingReportService;

    @Mock
    private ListingReportRepository listingReportRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingReportMapper listingReportMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("Create listing report test")
        class createReport {
            private final UUID listingId = UUID.randomUUID();

            @DisplayName("valid request should create listingReport and return listingReportResponse")
            @Test
            void validRequest_shouldReturnListingReportResponse() {
                ListingReportRequest listingReportRequest = new ListingReportRequest();
                listingReportRequest.setReason(ListingReportReason.FAKE_LISTING);
                listingReportRequest.setDescription("long long texted reason to pass validation");

                UUID hostId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                User host = new User();
                host.setUserId(hostId);

                User user = new User();
                user.setUserId(userId);

                Listing listing = new Listing();
                listing.setListingId(listingId);
                listing.setUser(host);

                ListingReportResponse expected = new ListingReportResponse();

                when(listingRepository.findByDetailedId(listingId)).thenReturn(Optional.of(listing));
                when(userService.getCurrentUserId()).thenReturn(userId);
                when(userRepository.getReferenceById(userId)).thenReturn(user);
                when(listingReportRepository.save(any(ListingReport.class))).thenReturn(new ListingReport());
                when(listingReportMapper.toListingReportResponse(any(ListingReport.class))).thenReturn(expected);

                ListingReportResponse result = listingReportService.createReport(listingId, listingReportRequest);

                ArgumentCaptor<ListingReport> captor = ArgumentCaptor.forClass(ListingReport.class);
                verify(listingReportRepository, times(1)).save(captor.capture());
                ListingReport listingReport = captor.getValue();

                assertThat(listingReport.getListing()).isEqualTo(listing);
                assertThat(listingReport.getUser()).isEqualTo(user);
                assertThat(listingReport.getReason()).isEqualTo(listingReportRequest.getReason());
                assertThat(listingReport.getStatus()).isEqualTo(ListingReportStatus.OPEN);
                assertThat(result).isEqualTo(expected);
            }

            @Test
            @DisplayName("listing not found should throw ListingNotFoundException")
            void listingNotFound_shouldThrowListingNotFoundException() {
                UUID listingId = UUID.randomUUID();
                ListingReportRequest lrr = new ListingReportRequest();
                when(listingRepository.findByDetailedId(listingId))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> listingReportService.createReport(listingId, lrr))
                        .isInstanceOf(ListingNotFoundException.class)
                        .hasMessage("Listing with id " + listingId + " not found");
            }

            @Test
            @DisplayName("Owner of the listing trying to report it")
            void ownerTryingReport_shouldThrowListingReportException() {
                UUID hostId = UUID.randomUUID();

                User host = new User();
                host.setUserId(hostId);

                Listing l = new Listing();
                l.setUser(host);
                ListingReportRequest lrr = new ListingReportRequest();

                when(listingRepository.findByDetailedId(listingId)).thenReturn(Optional.of(l));
                when(userService.getCurrentUserId()).thenReturn(hostId);

                assertThatThrownBy(() -> listingReportService.createReport(listingId, lrr))
                        .isInstanceOf(ListingReportException.class)
                        .hasMessage("You can't report your listing");
            }
        }

    @Nested
    @DisplayName("Patch listing report test")
    class patchReport {

        private final Long reportId = 1L;

        @DisplayName("valid request should patch report")
        @Test
        void Approved_shouldPatchReportAndReturnFullListingResponse() {
            Listing listing = new Listing();
            listing.setListingStatus(ListingStatus.APPROVED);
            ListingPatchRequest listingPatchRequest = new ListingPatchRequest();

            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setUserId(userId);
            user.setUsername("Test admin");

            listingPatchRequest.setStatus(ListingStatus.APPROVED);
            listingPatchRequest.setReviewedBy("reviewer. thank you for helping us!");
            listingPatchRequest.setListingReportStatus(ListingReportStatus.ACCEPTED);

            ListingReport lr = new ListingReport();
            lr.setListing(listing);
            lr.setStatus(ListingReportStatus.OPEN);

            FullListingReportResponse expected = new FullListingReportResponse();

            when(listingReportRepository.findListingReportByReportId(reportId))
                    .thenReturn(Optional.of(lr));
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(listingReportRepository.save(any(ListingReport.class))).thenReturn(lr);
            when(listingReportMapper.toFullListingReportResponse(any(ListingReport.class)))
                    .thenReturn(expected);

            FullListingReportResponse result = listingReportService.patchReport(reportId, listingPatchRequest);

            ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
            ArgumentCaptor<ListingReport> captor = ArgumentCaptor.forClass(ListingReport.class);
            verify(listingRepository, times(1)).save(listingCaptor.capture());
            verify(listingReportRepository, times(1)).save(captor.capture());
            ListingReport listingReport = captor.getValue();
            Listing l = listingCaptor.getValue();

            assertThat(expected).isEqualTo(result);
            assertThat(l.getListingStatus()).isEqualTo(ListingStatus.APPROVED);
            assertThat(listingReport.getListing()).isEqualTo(listing);
            assertThat(listingReport.getStatus()).isNotEqualTo(ListingReportStatus.OPEN);
            assertThat(listingReport.getReviewedBy()).isNotNull();
            assertThat(listingReport.getReviewedBy()).contains("Test admin");
            assertThat(listingReport.getResolvedAt()).isNotNull();
        }

        @DisplayName("valid request should patch report")
        @Test
        void Suspended_shouldPatchReportAndReturnFullListingResponse() {
            UUID listingId = UUID.randomUUID();

            Listing listing = new Listing();
            listing.setListingId(listingId);
            listing.setListingStatus(ListingStatus.PENDING);
            ListingPatchRequest listingPatchRequest = new ListingPatchRequest();

            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setUserId(userId);
            user.setUsername("Test admin");

            listingPatchRequest.setStatus(ListingStatus.SUSPENDED);
            listingPatchRequest.setReviewedBy("thank you for helping us!");
            listingPatchRequest.setListingReportStatus(ListingReportStatus.ACCEPTED);

            ListingReport lr = new ListingReport();
            lr.setListing(listing);
            lr.setStatus(ListingReportStatus.OPEN);

            FullListingReportResponse expected = new FullListingReportResponse();

            when(listingReportRepository.findListingReportByReportId(reportId))
                    .thenReturn(Optional.of(lr));
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(listingReportRepository.save(any(ListingReport.class))).thenReturn(lr);
            when(listingReportMapper.toFullListingReportResponse(any(ListingReport.class)))
                    .thenReturn(expected);

            FullListingReportResponse result = listingReportService.patchReport(reportId, listingPatchRequest);

            ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
            ArgumentCaptor<ListingReport> captor = ArgumentCaptor.forClass(ListingReport.class);
            verify(listingRepository, times(1)).save(listingCaptor.capture());
            verify(listingReportRepository, times(1)).save(captor.capture());
            ListingReport listingReport = captor.getValue();
            Listing l = listingCaptor.getValue();

            verify(listingReportRepository, times(1))
                    .resolveAllReports(eq(listingId),
                            eq(ListingReportStatus.OPEN),
                            eq(ListingReportStatus.AUTO_RESOLVED),
                            eq("Test admin.\nthank you for helping us!"),
                            any(LocalDateTime.class));
            verify(eventPublisher, times(1)).publishEvent(any(ListingSuspendedEvent.class));

            assertThat(expected).isEqualTo(result);
            assertThat(l.getListingStatus()).isEqualTo(ListingStatus.SUSPENDED);
            assertThat(listingReport.getListing()).isEqualTo(listing);
            assertThat(listingReport.getStatus()).isNotEqualTo(ListingReportStatus.OPEN);
            assertThat(listingReport.getReviewedBy()).isNotNull();
            assertThat(listingReport.getReviewedBy()).contains("Test admin");
            assertThat(listingReport.getResolvedAt()).isNotNull();
        }

        @DisplayName("listing report not found")
        @Test
        void listingNotFound_shouldThrowListingNotFoundException() {
            when(listingReportRepository.findListingReportByReportId(reportId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingReportService.patchReport(reportId, new ListingPatchRequest()))
                    .isInstanceOf(ListingReportNotFoundException.class)
                    .hasMessage("Report with id " + reportId + " not found");
        }

        @DisplayName("user not found")
        @Test
        void userNotFound_shouldThrowListingNotFoundException() {
            ListingPatchRequest lpr = new ListingPatchRequest();
            UUID userId = UUID.randomUUID();
            when(listingReportRepository.findListingReportByReportId(reportId))
                    .thenReturn(Optional.of(new ListingReport()));
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> listingReportService.patchReport(reportId, lpr))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with this id not exists");
        }

        @DisplayName("listing already SUSPENDED")
        @Test
        void listingAlreadySuspended_shouldThrowListingReportException() {
            ListingPatchRequest lpr = new ListingPatchRequest();
            ListingReport report = new ListingReport();
            report.setStatus(ListingReportStatus.AUTO_RESOLVED);
            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setUserId(userId);

            when(listingReportRepository.findListingReportByReportId(reportId))
                    .thenReturn(Optional.of(report));
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            assertThatThrownBy(() -> listingReportService.patchReport(reportId, lpr))
                    .isInstanceOf(ListingReportException.class)
                    .hasMessage("This report is already resolved");
        }
    }

    @Nested
    @DisplayName("Get listing report test")
    class getReport {
        @DisplayName("get my reports valid request")
        @Test
        void validRequest_shouldGetPageOfListingReportResponse() {
            Pageable pageable = PageRequest.of(0, 10);
            UUID userId = UUID.randomUUID();

            ListingReport lr1 = new ListingReport();
            ListingReport lr2 = new ListingReport();

            List<ListingReport> listingReports = new ArrayList<>();
            listingReports.add(lr1);
            listingReports.add(lr2);

            Page<ListingReport> page = new PageImpl<>(listingReports, pageable, listingReports.size());

            ListingReportResponse expected1 = new ListingReportResponse();
            ListingReportResponse expected2 = new ListingReportResponse();

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(listingReportRepository.findListingReportByUser_UserId(userId, pageable))
                    .thenReturn(page);
            when(listingReportMapper.toListingReportResponse(lr1))
                    .thenReturn(expected1);
            when(listingReportMapper.toListingReportResponse(lr2))
                    .thenReturn(expected2);

            Page<ListingReportResponse> result = listingReportService.myReports(PageRequest.of(0, 10));

            assertThat(result.getContent().get(0)).isEqualTo(expected1);
            assertThat(result.getContent().get(1)).isEqualTo(expected2);
        }

        @Test
        @DisplayName("get unreviewed reports")
        void validRequest_shouldGetUnreviewedReports() {
            Pageable pageable = PageRequest.of(0, 10);

            ListingReport lr1 = new ListingReport(); lr1.setStatus(ListingReportStatus.OPEN);
            ListingReport lr2 = new ListingReport(); lr2.setStatus(ListingReportStatus.OPEN);
            Page<ListingReport> page = new PageImpl<>(List.of(lr1,lr2), pageable, 2);

            FullListingReportResponse expected1 = new FullListingReportResponse();
            FullListingReportResponse expected2 = new FullListingReportResponse();

            when(listingReportRepository.findListingReportByStatus(ListingReportStatus.OPEN, pageable))
                    .thenReturn(page);
            when(listingReportMapper.toFullListingReportResponse(lr1)).thenReturn(expected1);
            when(listingReportMapper.toFullListingReportResponse(lr2)).thenReturn(expected2);

            Page<FullListingReportResponse> expectedPage = new PageImpl<>(List.of(expected1, expected2), pageable, 2);
            Page<FullListingReportResponse> result = listingReportService.getUnreviewedReports(pageable);

            assertThat(result).isEqualTo(expectedPage);
        }

        @Test
        @DisplayName("get report by id")
        void validRequest_shouldGetReportById() {
            ListingReport lr = new ListingReport();
            FullListingReportResponse expectedlr = new FullListingReportResponse();
            Long reportId = 1L;
            when(listingReportRepository.findListingReportByReportId(reportId))
                    .thenReturn(Optional.of(lr));
            when(listingReportMapper.toFullListingReportResponse(lr))
                    .thenReturn(expectedlr);

            FullListingReportResponse result =  listingReportService.getReport(reportId);

            assertThat(result).isEqualTo(expectedlr);
        }

        @Test
        @DisplayName("Report not found should throw EntityNotFoundException")
        void reportNotFound_shouldThrowEntityNotFoundException() {
            Long reportId = 1L;
            when(listingReportRepository.findListingReportByReportId(reportId))
                    .thenReturn(Optional.empty());
            assertThatThrownBy(() -> listingReportService.getReport(reportId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Report with id " + reportId + " not found");
        }
    }
}
