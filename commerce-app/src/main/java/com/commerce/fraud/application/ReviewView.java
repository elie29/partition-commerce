package com.commerce.fraud.application;

import com.commerce.fraud.domain.Review;
import com.commerce.fraud.domain.ReviewStatus;
import java.time.Instant;
import java.util.UUID;

/** A review as a read shows it (BR-8): no {@code customerId}; {@code liftedAt} is empty ({@code null}) while open. */
public record ReviewView(UUID reviewId, ReviewStatus status, Instant placedAt, Instant liftedAt) {

    static ReviewView of(Review review) {
        return new ReviewView(review.reviewId(), review.status(), review.placedAt(), review.liftedAt());
    }

    @Override
    public String toString() {
        return "ReviewView[" + reviewId + "]";
    }
}
