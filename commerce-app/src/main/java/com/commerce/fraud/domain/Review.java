package com.commerce.fraud.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * A fraud review: the risk team places a customer, named by the {@code customerId} billing holds, under it at
 * {@code placedAt}, and lifts it at {@code liftedAt}, empty ({@code null}) while open. {@link #place} makes it open;
 * {@link #lift} is the one move of its status, and nothing makes it open again (BR-1, BR-5, P-7).
 */
public record Review(UUID reviewId, String customerId, ReviewStatus status, Instant placedAt, Instant liftedAt) {

    /** A new review, open; the {@code customerId} bounded by BR-7. */
    public static Review place(UUID reviewId, CustomerId customerId, Instant placedAt) {
        return new Review(reviewId, customerId.value(), ReviewStatus.OPEN, placedAt, null);
    }

    /** The review lifted at {@code liftedAt}; a review already lifted stays as it is. */
    public Review lift(Instant liftedAt) {
        return isOpen() ? new Review(reviewId, customerId, ReviewStatus.LIFTED, placedAt, liftedAt) : this;
    }

    public boolean isOpen() {
        return status == ReviewStatus.OPEN;
    }

    /** The {@code customerId} is never written to a log (BR-9). */
    @Override
    public String toString() {
        return "Review[" + reviewId + "]";
    }
}
