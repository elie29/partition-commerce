package com.commerce.fraud;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerce.fraud.application.CustomerStatus;
import com.commerce.fraud.application.LiftReviewUseCase;
import com.commerce.fraud.application.PlaceReviewUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The use case behind {@code CustomerStatus}, the contract {@code fraud} shows {@code payment}: BR-3 and BR-4 of the
 * spec of {@code fraud}. A customer is blocked while a review of it is open, not lifted.
 */
@FraudTest
class CustomerStatusTest {

    @Autowired
    CustomerStatus customerStatus;

    @Autowired
    PlaceReviewUseCase placeReview;

    @Autowired
    LiftReviewUseCase liftReview;

    @Test
    void a_customer_under_review_is_blocked() {
        String customerId = freshCustomer();

        placeReview.place(customerId);

        assertThat(customerStatus.blocked(customerId)).isTrue();
    }

    @Test
    void a_customer_whose_review_is_lifted_is_not_blocked() {
        String customerId = freshCustomer();
        placeReview.place(customerId);

        liftReview.lift(customerId);

        assertThat(customerStatus.blocked(customerId)).isFalse();
    }

    @Test
    void a_customer_never_placed_is_not_blocked() {
        assertThat(customerStatus.blocked(freshCustomer())).isFalse();
        assertThat(customerStatus.blocked("")).isFalse();
    }

    @Test
    void a_customer_placed_again_after_a_lift_is_blocked_again() {
        String customerId = freshCustomer();
        placeReview.place(customerId);
        liftReview.lift(customerId);

        placeReview.place(customerId);

        assertThat(customerStatus.blocked(customerId)).isTrue();
    }

    @Test
    void the_customer_is_asked_as_the_same_text() {
        String customerId = "Customer-" + UUID.randomUUID();
        placeReview.place(customerId);

        assertThat(customerStatus.blocked(customerId.toLowerCase())).isFalse();
        assertThat(customerStatus.blocked(" " + customerId)).isFalse();
    }

    private static String freshCustomer() {
        return "customer-" + UUID.randomUUID();
    }
}
