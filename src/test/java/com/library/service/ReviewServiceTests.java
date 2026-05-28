package com.library.service;

import com.library.dto.review.ReviewRequest;
import com.library.dto.review.ReviewResponse;
import com.library.dto.review.UpdateReviewRequest;
import com.library.entity.Listing;
import com.library.entity.Review;
import com.library.entity.User;
import com.library.enums.Status;
import com.library.mapper.ReviewMapper;
import com.library.repository.BookingRepository;
import com.library.repository.ListingRepository;
import com.library.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTests {
    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @Mock
    private ListingService listingService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Nested
    @DisplayName("get reviews")
    class getReview {
        @Test
        @DisplayName("get All reviews of listing succesfully valid request should return ReviewResponse")
        void validRequest_shouldReturnReviewResponse() {
            UUID listingId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 3);

            Review review1 = Review.builder().reviewId(UUID.randomUUID()).comment("good.").build();
            Review review2 = Review.builder().reviewId(UUID.randomUUID()).comment("bad.").build();
            Review review3 = Review.builder().reviewId(UUID.randomUUID()).comment("not bad. Normally more accurate").build();

            List<Review> reviews = List.of(
                    review1, review2, review3
            );
            Page<Review> page = new PageImpl<>(reviews, pageable, reviews.size());

            when(listingRepository.findById(listingId)).thenReturn(Optional.of(new Listing()));
            when(reviewRepository.findAllByListing_ListingIdOrderByCreatedAtDesc(listingId, pageable))
                    .thenReturn(page);

            when(reviewMapper.toReviewResponse(any(Review.class))).thenReturn(new ReviewResponse());

            Page<ReviewResponse> result = reviewService.getAllReviewsOfListing(listingId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent().size()).isEqualTo(3);
            assertThat(result.getTotalElements()).isEqualTo(3);

            verify(reviewRepository, times(1))
                    .findAllByListing_ListingIdOrderByCreatedAtDesc(listingId, pageable);

            verify(reviewMapper, times(3))
                    .toReviewResponse(any(Review.class));
        }

        @Test
        @DisplayName("Listing not found entity not found should throw entity not found")
        void listingNotFound_shouldThrowEntityNotFoundException() {
            UUID listingId = UUID.randomUUID();

            when(listingRepository.findById(listingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.getAllReviewsOfListing(listingId, PageRequest.of(0, 1)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("listing not found");
        }
    }

//    all cases
//    1. if everything is good, and you add review and get ReviewResponse
//    2. if listingService.getListingOrThrow threw entity not found catch it up
//    3. if you are owner of this listing so you cannot rate your own listing should throw new IllegalStateException()
//    4. if booking not found so they throw Entity not found exp
//    5. if user trying to create more review than one
    @Nested
    @DisplayName("create reviews")
    class postReviews {
        @Test
        @DisplayName("should succesfully add review and update listing stats")
        void validRequest_shouldReturnReviewResponse() {
            UUID userId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();

            User user = new User();
            user.setUserId(userId);
            user.setEmail("test@gmail.com");

            User owner = new User();
            owner.setUserId(ownerId);

            Listing listing = new Listing();
            listing.setListingId(listingId);
            listing.setUser(owner);
            listing.setReviewsCount(0L);
            listing.setAverageRating(BigDecimal.ZERO);

            ReviewRequest reviewRequest = new ReviewRequest();
            reviewRequest.setRating(BigDecimal.valueOf(5));
            reviewRequest.setComment("Good");


            when(userService.getCurrentUserEmail()).thenReturn(user.getEmail());
            when(userService.getUserByEmail(any())).thenReturn(user);
            when(listingService.getListingOrThrow(listingId))
                    .thenReturn(listing);
            when(bookingRepository.existsByListing_ListingIdAndUser_UserIdAndStatus(listingId, userId, Status.COMPLETED))
                    .thenReturn(true);
            when(reviewRepository.countAllByListing_ListingIdAndUser_UserId(listingId, userId))
                    .thenReturn(0L);

            when(reviewMapper.toReviewResponse(any(Review.class)))
                    .thenAnswer(i -> {
                        Review s = i.getArgument(0);
                        return ReviewResponse.builder().comment(s.getComment()).rating(s.getRating()).build();
                    });

            ReviewResponse response = reviewService.addReview(reviewRequest, listingId);

            assertThat(response).isNotNull();
            assertThat(response.getComment()).isEqualTo("Good");

            ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
            verify(reviewRepository, times(1)).save(captor.capture());

            Review savedReview = captor.getValue();
            assertThat(savedReview.getListing().getReviewsCount()).isEqualTo(1L);
            assertThat(savedReview.getListing().getAverageRating()).isEqualTo("5.00");
        }

        @Test
        @DisplayName("listing not found tcatch entity not found")
        void listingNotFound_shouldThrowEntityNotFoundException() {
            ReviewRequest reviewRequest = new ReviewRequest();

            UUID listingId = UUID.randomUUID();
            when(listingService.getListingOrThrow(listingId))
                    .thenThrow(new EntityNotFoundException("listing not exists"));

            assertThatThrownBy(() -> reviewService.addReview(reviewRequest, listingId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("listing not exists");

            verify(reviewRepository, never()).save(any(Review.class));
            verify(listingRepository, never()).save(any(Listing.class));
        }


        @Test
        @DisplayName("owner trying to rate his own listing illegal state exception")
        void ownerCannotRateHisOwnListing_shouldThrowIllegalStateException() {
            UUID userId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();
            ReviewRequest reviewRequest = new ReviewRequest();

            User owner = new User();
            owner.setUserId(userId);
            owner.setEmail("test@gmail.com");
            Listing listing = new Listing();
            listing.setUser(owner);

            when(userService.getCurrentUserEmail()).thenReturn(owner.getEmail());
            when(userService.getUserByEmail(any(String.class))).thenReturn(owner);
            when(listingService.getListingOrThrow(listingId)).thenReturn(listing);

            assertThatThrownBy(() -> reviewService.addReview(reviewRequest, listingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You cannot add reviews to your listing");

            verify(reviewRepository, never()).save(any(Review.class));
            verify(listingRepository, never()).save(any(Listing.class));
        }
        //    4. if booking not found so they throw Entity not found exp

        @Test
        @DisplayName("you cannot review due to you never booked this listing should throw IllegalStateException")
        void bookingNotFound_shouldThrowIllegalStateException() {
            UUID userId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();
            ReviewRequest reviewRequest = new ReviewRequest();

            User owner = new User();
            owner.setUserId(ownerId);

            User user = new User();
            user.setUserId(userId);
            user.setEmail("test@gmail.com");

            Listing listing = new Listing();
            listing.setListingId(listingId);
            listing.setUser(owner);

            when(listingService.getListingOrThrow(listingId)).thenReturn(listing);
            when(userService.getCurrentUserEmail()).thenReturn(user.getEmail());
            when(userService.getUserByEmail(any(String.class))).thenReturn(user);

            assertThatThrownBy(() -> reviewService.addReview(reviewRequest, listingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You must have a completed booking to leave a review");

            verify(reviewRepository, never()).save(any(Review.class));
            verify(listingRepository, never()).save(any(Listing.class));
        }

        @Test
        @DisplayName("you cannot review more than once should throw IllegalStateException")
        void haveMoreThanOneReview_shouldThrowIllegalStateException() {
            UUID userId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();

            User owner = new User();
            owner.setUserId(ownerId);

            User user = new User();
            user.setUserId(userId);
            user.setEmail("test@gmail.com");

            Listing listing = new Listing();
            listing.setListingId(listingId);
            listing.setUser(owner);

            ReviewRequest reviewRequest = new ReviewRequest();

            when(userService.getCurrentUserEmail())
                    .thenReturn(user.getEmail());
            when(userService.getUserByEmail(any(String.class)))
                    .thenReturn(user);
            when(listingService.getListingOrThrow(listingId)).thenReturn(listing);
            when(bookingRepository.existsByListing_ListingIdAndUser_UserIdAndStatus(listingId, user.getUserId(), Status.COMPLETED))
                    .thenReturn(true);
            when(reviewRepository.countAllByListing_ListingIdAndUser_UserId(listingId, user.getUserId()))
                    .thenReturn(1L);

            assertThatThrownBy(() -> reviewService.addReview(reviewRequest, listingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You can't review anymore");
        }
    }

    @Nested
    @DisplayName("update reviews")
    class updateReview {
//        1. if everything was correct, and it should update review
//        2. if review not found
//        3. user not own this review

        @Test
        @DisplayName("update review succesfully")
        void updateReview_successfully() {
            UUID reviewId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();
            UpdateReviewRequest urr = new UpdateReviewRequest();
            urr.setComment("Updated comment!");
            urr.setRating(new BigDecimal("5.00"));

            User user = new User();
            user.setEmail("user@gmail.com");

            Listing listing = new Listing();
            listing.setAverageRating(new BigDecimal("4.5"));
            listing.setReviewsCount(2L);
            listing.setListingId(listingId);

            Review review = new Review();
            review.setRating(new BigDecimal("4.00"));
            review.setUser(user);
            review.setListing(listing);

            when(listingRepository.existsById(listingId)).thenReturn(true);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(userService.getCurrentUserEmail()).thenReturn(user.getEmail());

            doAnswer(invocation -> {
                UpdateReviewRequest req = invocation.getArgument(0);
                Review rev = invocation.getArgument(1);
                rev.setComment(req.getComment());
                rev.setRating(req.getRating());
                return null;
            }).when(reviewMapper).updateReview(any(UpdateReviewRequest.class), any(Review.class));

            when(listingRepository.existsById(listingId)).thenReturn(true);

            when(reviewMapper.toReviewResponse(any(Review.class)))
                    .thenReturn(ReviewResponse.builder()
                            .rating(urr.getRating())
                            .comment(urr.getComment())
                            .build());

            ReviewResponse result = reviewService.updateReview(urr, listingId, reviewId);

            assertThat(result).isNotNull();
            assertThat(result.getComment()).isEqualTo("Updated comment!");
            assertThat(result.getRating()).isEqualTo(new BigDecimal("5.00"));

            assertThat(review.getRating()).isEqualByComparingTo("5.00");
            assertThat(review.getComment()).isEqualTo("Updated comment!");

            ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
            verify(listingRepository, times(1)).save(listingCaptor.capture());

            Listing saved = listingCaptor.getValue();
            assertThat(saved.getAverageRating()).isEqualByComparingTo("5.00");
            assertThat(saved.getReviewsCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Listing not found should throw entity not found")
        void listingNotFound_shouldThrowEntityNotFound() {
            UpdateReviewRequest urr = new UpdateReviewRequest();
            UUID reviewId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();

            when(listingRepository.existsById(listingId)).thenReturn(false);

            assertThatThrownBy(() -> {reviewService.updateReview(urr, listingId, reviewId);})
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Listing not exists");
        }

        @Test
        @DisplayName("Review not found should throw entity not found")
        void reviewNotFound_shouldThrowEntityNotFound() {
            UpdateReviewRequest urr = new UpdateReviewRequest();
            UUID reviewId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
            when(listingRepository.existsById(listingId)).thenReturn(true);

            assertThatThrownBy(() -> reviewService.updateReview(urr, listingId, reviewId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(listingRepository, never()).save(any(Listing.class));
            verify(reviewMapper, never()).updateReview(any(UpdateReviewRequest.class), any(Review.class));
        }

        @Test
        @DisplayName("User not owner of review")
        void notOwnerOfReview_shouldThrowIllegalStateException() {
            UpdateReviewRequest urr = new UpdateReviewRequest();
            UUID reviewId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();

            User owner = new User();
            owner.setEmail("owner@gmail.com");

            User user = new User();
            user.setEmail("user@gmail.com");

            Review review = new Review();
            review.setUser(owner);
            review.setReviewId(reviewId);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(userService.getCurrentUserEmail()).thenReturn(user.getEmail());
            when(listingRepository.existsById(listingId))
                    .thenReturn(true);

            assertThatThrownBy(() -> reviewService.updateReview(urr, listingId, reviewId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Not your review!");

            verify(listingRepository, never()).save(any(Listing.class));
            verify(reviewMapper, never()).updateReview(any(UpdateReviewRequest.class), any(Review.class));
        }
    }

    @Nested
    @DisplayName("delet reviews")
    class deleteReview {
//        1. Successfully deleted this review
//        2. Not found review
//        2. You aren't owner of this review
        @Test
        @DisplayName("delete reviews successful")
        void shouldDeleteReviewSuccessfully() {
            UUID reviewId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();

            User user = new User();
            user.setEmail("test@gmail.com");

            Listing listing = new Listing();
            listing.setAverageRating(new BigDecimal("4.5"));
            listing.setReviewsCount(2L);

            Review review = new Review();
            review.setRating(new BigDecimal("4.00"));
            review.setReviewId(reviewId);
            review.setListing(listing);
            review.setUser(user);

            when(listingService.getListingOrThrow(listingId)).thenReturn(listing);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(userService.getCurrentUserEmail()).thenReturn(user.getEmail());

            reviewService.deleteReview(reviewId, listingId);

            ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
            verify(listingRepository).save(listingCaptor.capture());

            Listing saved = listingCaptor.getValue();
            assertThat(saved.getReviewsCount()).isEqualTo(1L);
            assertThat(saved.getAverageRating()).isEqualTo(new BigDecimal("5.00"));

            verify(reviewRepository, times(1)).delete(review);
        }
        @Test
        @DisplayName("review not found should throw EntityNotFound")
        void reviewNotFound_shouldThrowEntityNotFound() {
            UUID reviewId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> reviewService.deleteReview(reviewId, listingId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(listingRepository, never()).save(any(Listing.class));
            verify(reviewRepository, never()).delete(any(Review.class));
        }

        @Test
        @DisplayName("you are not owner of this review should throw IllegalStateException")
        void notOwner_shouldThrowIllegalStateException() {
            UUID reviewId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();

            User owner = new User();
            owner.setEmail("owner@gmail.com");

            User user = new User();
            user.setEmail("user@gmail.com");

            Review review = new Review();
            review.setUser(owner);

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(userService.getCurrentUserEmail()).thenReturn(user.getEmail());

            assertThatThrownBy(() -> {reviewService.deleteReview(reviewId, listingId);})
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Not your review!");

            verify(listingRepository, never()).save(any(Listing.class));
            verify(reviewRepository, never()).delete(any(Review.class));
        }

        @Test
        @DisplayName("Listing not found should throw Entity Not Found")
        void notListing_shouldThrowEntityNotFound() {
            UUID reviewId = UUID.randomUUID();
            UUID listingId = UUID.randomUUID();

            User user = new User();
            user.setEmail("test@gmail.com");

            Review review = new Review();
            review.setUser(user);

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(userService.getCurrentUserEmail()).thenReturn(user.getEmail());
            when(listingService.getListingOrThrow(listingId))
                    .thenThrow(new EntityNotFoundException("entity not exists"));

            assertThatThrownBy(() -> {reviewService.deleteReview(reviewId, listingId);})
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("entity not exists");
        }
    }
}
