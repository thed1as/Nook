package com.nooki.controller;

import com.nooki.dto.listingReport.FullListingReportResponse;
import com.nooki.dto.listingReport.ListingPatchRequest;
import com.nooki.dto.listingReport.ListingReportRequest;
import com.nooki.dto.listingReport.ListingReportResponse;
import com.nooki.enums.listingReport.ListingReportReason;
import com.nooki.enums.listingReport.ListingReportStatus;
import com.nooki.enums.listingReport.ListingStatus;
import com.nooki.service.ListingServices.ListingReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListingReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testing listing report controller")
public class ListingReportControllerTest extends AbstractControllerTest{
    @MockitoBean
    private ListingReportService listingReportService;

    @Nested
    @DisplayName("Create")
    class createReport {
        private final UUID listingId = UUID.randomUUID();
        private final String URL = "/api/v1/listings/" + listingId + "/reports";
        @Test
        @DisplayName("Valid request should create report and return 201")
        void validRequest_shouldCreateReportAndReturn201() throws Exception {
            ListingReportRequest request = new ListingReportRequest();
            request.setReason(ListingReportReason.FAKE_LISTING);
            request.setDescription("long long texted reason to pass validation");

            ListingReportResponse response = new ListingReportResponse();
            response.setStatus(ListingReportStatus.OPEN);
            response.setReason(ListingReportReason.FAKE_LISTING);
            response.setReviewedBy("long long texted reason to pass validation");

            when(listingReportService.createReport(eq(listingId), any(ListingReportRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.reason").value("FAKE_LISTING"))
                    .andExpect(jsonPath("$.reviewedBy")
                            .value("long long texted reason to pass validation"));

            verify(listingReportService)
                    .createReport(eq(listingId), any(ListingReportRequest.class));
        }
        @Test
        @DisplayName("Valid request should return 400 report")
        void BadCredential_AndShouldReturn400() throws Exception {
            ListingReportRequest listingReportRequest = new ListingReportRequest();
            listingReportRequest.setReason(ListingReportReason.FAKE_LISTING);
            listingReportRequest.setDescription("long");
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listingReportRequest))
            ).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get / Patch reports admin")
    class adminGetReports {
        @Test
        @DisplayName("Valid request should return open reports")
        void validRequest_shouldReturnOpenReports() throws Exception {
            String URL = "/api/v1/reports";
            FullListingReportResponse resp1 = new FullListingReportResponse();
            resp1.setStatus(ListingReportStatus.OPEN);
            FullListingReportResponse resp2 = new FullListingReportResponse();
            resp2.setStatus(ListingReportStatus.ACCEPTED);

            Page<FullListingReportResponse> expected =
                    new PageImpl<>(List.of(resp1, resp2), PageRequest.of(0, 10), 2);

            when(listingReportService.getUnreviewedReports(any(Pageable.class)))
                    .thenReturn(expected);

            mockMvc.perform(get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$.content.length()").value(2)
            ).andExpect(jsonPath("$.content[0].status").value("OPEN"));
        }
        @Test
        @DisplayName("Valid request should return report by id")
        void validRequest_AndShouldReturnReport() throws Exception {
            String URL = "/api/v1/reports/1";
            FullListingReportResponse resp = new FullListingReportResponse();
            resp.setStatus(ListingReportStatus.ACCEPTED);

            when(listingReportService.getReport(any(Long.class)))
                    .thenReturn(resp);

            mockMvc.perform(get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$.status").value("ACCEPTED"));
        }

        @Test
        @DisplayName("Valid request should patch report by id")
        void validRequest_shouldPatchReport() throws Exception {
            String URL = "/api/v1/reports/1/resolve";
            ListingPatchRequest lpr = new ListingPatchRequest();

            lpr.setStatus(ListingStatus.APPROVED);
            lpr.setReviewedBy("reviewer. thank you for helping us!");
            lpr.setListingReportStatus(ListingReportStatus.ACCEPTED);

            FullListingReportResponse resp = new FullListingReportResponse();
            resp.setStatus(ListingReportStatus.ACCEPTED);

            when(listingReportService.patchReport(any(Long.class), any(ListingPatchRequest.class)))
                    .thenReturn(resp);

            mockMvc.perform(patch(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(lpr))
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$.status").value("ACCEPTED"));

            verify(listingReportService, times(1)).patchReport(any(Long.class), any(ListingPatchRequest.class));
        }
    }

    @Nested
    @DisplayName("Get for user")
    class userGetMyReports {
        @DisplayName("valid request should return page of ListingReportResponse")
        @Test
        void validRequest_shouldReturnPageOfListingReportResponse() throws Exception {
            String URL = "/api/v1/reports/me";

            ListingReportResponse resp1 = new ListingReportResponse();
            resp1.setStatus(ListingReportStatus.OPEN);

            ListingReportResponse resp2 = new ListingReportResponse();
            resp2.setStatus(ListingReportStatus.ACCEPTED);

            Page<ListingReportResponse> page =
                    new PageImpl<>(List.of(resp1, resp2), PageRequest.of(0, 10), 2);

            when(listingReportService.myReports(any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get(URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk()
                    ).andExpect(jsonPath("$.content.length()").value(2)
                    ).andExpect(jsonPath("$.content[0].status").value("OPEN"));
        }
    }

}
