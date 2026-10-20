package com.commerce.fraud.application;

import java.util.List;
import org.springframework.stereotype.Service;

/** Reads a customer's fraud reviews: BR-8 of the spec of {@code fraud}. No operation exposes it in this version. */
@Service
public class ReadReviewsUseCase {

    private final Reviews reviews;

    public ReadReviewsUseCase(Reviews reviews) {
        this.reviews = reviews;
    }

    /** The reviews of the customer named by the text as it is, by time placed, oldest first; none when never placed. */
    public List<ReviewView> reviews(String customerId) {
        return reviews.ofCustomer(customerId).stream().map(ReviewView::of).toList();
    }
}
