package com.library.mapper;

import com.library.dto.review.ReviewRequest;
import com.library.dto.review.ReviewResponse;
import com.library.dto.review.UpdateReviewRequest;
import com.library.entity.Review;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReviewMapper {

    @Mapping(target = "username", source = "user.username")
    ReviewResponse toReviewResponse(Review review);

    Review toReview(ReviewRequest reviewRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReview(UpdateReviewRequest updateReviewRequest, @MappingTarget Review review);
}
