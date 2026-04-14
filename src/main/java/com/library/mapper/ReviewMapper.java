package com.library.mapper;

import com.library.dto.review.ReviewRequest;
import com.library.dto.review.ReviewResponse;
import com.library.entity.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewResponse toReviewResponse(Review review);

    Review toReview(ReviewRequest reviewRequest);
}
