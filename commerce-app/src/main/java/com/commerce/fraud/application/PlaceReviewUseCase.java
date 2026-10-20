package com.commerce.fraud.application;

import com.commerce.fraud.domain.CustomerId;
import com.commerce.fraud.domain.Review;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Places a customer under fraud review: BR-5, BR-6 and BR-7 of the spec of {@code fraud}. No operation exposes it in
 * this version, and nothing is checked about who calls it.
 */
@Service
public class PlaceReviewUseCase {

    private final Reviews reviews;
    private final Clock clock;

    public PlaceReviewUseCase(Reviews reviews, Clock clock) {
        this.reviews = reviews;
        this.clock = clock;
    }

    /**
     * A customer already under review is left as it is, nothing written; of two places at the same moment, the first
     * to commit holds and the other changes nothing (BR-6).
     *
     * @throws com.commerce.fraud.domain.InvalidCustomerId when BR-7 refuses the text, nothing placed
     */
    public void place(String customerId) {
        CustomerId customer = new CustomerId(customerId);
        if (reviews.findOpen(customer.value()).isPresent()) {
            return;
        }
        Instant placedAt = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS);
        try {
            reviews.add(Review.place(UUID.randomUUID(), customer, placedAt));
        } catch (OpenReviewHeld e) {
            // Another place of the same customer committed first: nothing changes (BR-6).
        }
    }
}
