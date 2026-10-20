package com.commerce.fraud.application;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

/**
 * Lifts a customer's fraud review: BR-1 of the spec of {@code fraud}. No operation exposes it in this version, and
 * nothing is checked about who calls it.
 */
@Service
public class LiftReviewUseCase {

    private final Reviews reviews;
    private final Clock clock;

    public LiftReviewUseCase(Reviews reviews, Clock clock) {
        this.reviews = reviews;
        this.clock = clock;
    }

    /** The customer, named by the text as it is, with no open review: nothing changes. */
    public void lift(String customerId) {
        reviews.findOpen(customerId).ifPresent(open ->
                reviews.lift(open.lift(Instant.now(clock).truncatedTo(ChronoUnit.MILLIS))));
    }
}
