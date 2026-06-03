package com.library.controller;

import com.library.config.SecurityConfig;
import com.library.dto.review.ReviewRequest;
import com.library.dto.review.ReviewResponse;
import com.library.dto.review.UpdateReviewRequest;
import com.library.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import({SecurityConfig.class})
@DisplayName("Testing Review Controller")
public class ReviewControllerTests extends AbstractControllerTest {
    @MockitoBean
    private ReviewService reviewService;

    @Nested
    @DisplayName("get Reviews (/listing/{id}/reviews")
    class GetReviews {
        private final UUID listingId = UUID.randomUUID();
        private final String URL = "/api/listing/" + listingId + "/reviews";

        @Test
        @DisplayName("Valid request should return 200 and page of review response for hosts")
        @WithMockUser(roles = "HOST")
        void validHostRequest_shouldReturn200_andPageOfReviewResponses() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            ReviewResponse r1 = new ReviewResponse();
            ReviewResponse r2 = new ReviewResponse();
            List<ReviewResponse> llr = List.of(r1, r2);
            Page<ReviewResponse> reviewResponses = new PageImpl<>(llr, pageable, llr.size());

            when(reviewService.getAllReviewsOfListing(listingId, pageable)).thenReturn(reviewResponses);

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("page", "0")
                    .param("size", "10")
            ).andExpect(status().isOk());
//            Should I check something more or what
        }

        @Test
        @DisplayName("Valid request should return 200 and page of review response for hosts")
        @WithMockUser(roles = "USER")
        void validUserRequest_shouldReturn200_andPageOfReviewResponses() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            ReviewResponse r1 = new ReviewResponse();
            ReviewResponse r2 = new ReviewResponse();

            List<ReviewResponse> lrr = List.of(r1,r2);
            Page<ReviewResponse> reviewResponses = new PageImpl<>(lrr, pageable, lrr.size());

            when(reviewService.getAllReviewsOfListing(eq(listingId), any(Pageable.class)))
                    .thenReturn(reviewResponses);

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("page", "0")
                    .param("size", "10")
            ).andExpect(status().isOk());
        }

        @Test
        @DisplayName("Anonymous user should return 403")
        void anonymousUser_ShouldReturn403() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            ReviewResponse r1 = new ReviewResponse();
            ReviewResponse r2 = new ReviewResponse();

            mockMvc.perform(get(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("page", "0")
                    .param("size", "10")
            ).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("create Reviews (/listing/{id}/reviews)")
    class CreateReviews {
        private final UUID listingId = UUID.randomUUID();
        private final String URL = "/api/listing/" + listingId + "/reviews";

        @Test
        @DisplayName("valid request should return 201 (ReviewResponse) and create Review")
        @WithMockUser(roles = "USER")
        void validRequest_shouldReturn201_andCreateReview() throws Exception {
            ReviewRequest reviewRequest = new ReviewRequest();
            reviewRequest.setRating(new BigDecimal("5.00"));
            reviewRequest.setComment("test comment");

            ReviewResponse expectedResponse = new ReviewResponse();
            expectedResponse.setComment("test comment");
            expectedResponse.setRating(new BigDecimal("5.00"));

            when(reviewService.addReview(any(ReviewRequest.class), eq(listingId)))
                    .thenReturn(expectedResponse);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewRequest))
            ).andExpect(status().isCreated()
            ).andExpect(jsonPath("$.comment").value("test comment")
            ).andExpect(jsonPath("$.rating").value("5.0"));
        }

        @Test
        @DisplayName("Return 400, rating invalid data")
        @WithMockUser(roles = "USER")
        void incorrectData_ShouldReturn400() throws Exception {
            ReviewRequest reviewRequest = new ReviewRequest();
            reviewRequest.setRating(new BigDecimal("6.00"));
            reviewRequest.setComment("test comment");

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewRequest))
            ).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Return 403, forbidden user or unauthorized")
        void anonymousUser_ShouldReturn403() throws Exception {
            ReviewRequest reviewRequest = new ReviewRequest();
            reviewRequest.setRating(new BigDecimal("3.00"));
            mockMvc.perform(put(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewRequest))
            ).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("update Reviews (/listing/{id}/reviews")
    class UpdateReviews {
        private final UUID listingId = UUID.randomUUID();
        private final UUID reviewId = UUID.randomUUID();
        private final String URL = "/api/listing/" + listingId + "/reviews/" + reviewId;

        @Test
        @DisplayName("valid request should update an review")
        @WithMockUser(roles = "USER")
        void validRequest_shouldUpdateReview() throws Exception{
            UpdateReviewRequest updateReviewRequest = new UpdateReviewRequest();
            updateReviewRequest.setRating(new BigDecimal("5.00"));
            updateReviewRequest.setComment("test comment");

            ReviewResponse expectedResponse = new ReviewResponse();
            expectedResponse.setComment("test comment");
            expectedResponse.setRating(new BigDecimal("5.00"));

            when(reviewService.updateReview(updateReviewRequest, reviewId))
                    .thenReturn(expectedResponse);

            mockMvc.perform(put(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateReviewRequest))
            ).andExpect(status().isOk()
            ).andExpect(jsonPath("$.comment").value("test comment")
            ).andExpect(jsonPath("$.rating").value("5.0"));
        }

        @Test
        @DisplayName("Return 400, invalid data")
        @WithMockUser(roles = "USER")
        void incorrectData_shouldReturn400() throws Exception {
            UpdateReviewRequest updateReviewRequest = new UpdateReviewRequest();
            updateReviewRequest.setRating(new BigDecimal("6.00"));
            updateReviewRequest.setComment("test comment");

            mockMvc.perform(put(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateReviewRequest))
            ).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Return 403, anonymous user or unauthorized")
        void anonymousUser_ShouldReturn403() throws Exception {
            UpdateReviewRequest updateReviewRequest = new UpdateReviewRequest();
            updateReviewRequest.setRating(new BigDecimal("2.00"));
            updateReviewRequest.setComment("test comment");

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateReviewRequest))
            ).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("delete Reviews (/listing/{id}/reviews")
    class DeleteReviews {
        private final UUID listingId = UUID.randomUUID();
        private final UUID reviewId = UUID.randomUUID();
        private final String URL = "/api/listing/" + listingId + "/reviews/" + reviewId;

        @Test
        @DisplayName("valid request should delete listing")
        @WithMockUser(roles = "USER")
        void validRequest_shouldReturn200AndDeleteListing() throws Exception {
            mockMvc.perform(delete(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("anonymous or unauthorized user should return 403")
        void anonymousUser_shouldReturn403() throws Exception {
            mockMvc.perform(delete(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden());
        }
    }
}
