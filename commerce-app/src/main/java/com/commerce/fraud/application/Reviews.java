package com.commerce.fraud.application;

import com.commerce.fraud.domain.Review;
import java.util.List;
import java.util.Optional;

/** The fraud reviews of the store, the port {@code fraud}'s outbound adapter implements. */
public interface Reviews {

    /**
     * Records a new open review; the store holds one open review per customer (BR-6, P-8), so of two at the same
     * moment the first to commit holds.
     *
     * @throws OpenReviewHeld when the store already holds an open review for the customer
     */
    void add(Review review);

    /** The open review of the customer named by this text, as it is. */
    Optional<Review> findOpen(String customerId);

    /** Writes a review lifted, if the store still holds it open. */
    void lift(Review lifted);

    /** The reviews of the customer named by this text, as it is, by time placed, oldest first (BR-8). */
    List<Review> ofCustomer(String customerId);
}
