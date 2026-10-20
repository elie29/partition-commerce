package com.commerce.fraud;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerce.fraud.application.LiftReviewUseCase;
import com.commerce.fraud.application.PlaceReviewUseCase;
import com.commerce.fraud.application.ReadReviewsUseCase;
import com.commerce.fraud.application.ReviewView;
import com.commerce.fraud.domain.ReviewStatus;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The use case "read a customer's fraud reviews": BR-8 and BR-9 of the spec of {@code fraud}. Only its tests reach the
 * use case in this version.
 */
@FraudTest
class ReadReviewsTest {

    @Autowired
    PlaceReviewUseCase placeReview;

    @Autowired
    LiftReviewUseCase liftReview;

    @Autowired
    ReadReviewsUseCase readReviews;

    @Test
    void a_read_lists_the_customers_reviews_by_time_placed_oldest_first_with_their_identifier_status_and_times()
            throws InterruptedException {
        String customerId = freshCustomer();
        placeReview.place(customerId);
        Thread.sleep(5);
        liftReview.lift(customerId);
        Thread.sleep(5);
        placeReview.place(customerId);

        List<ReviewView> reviews = readReviews.reviews(customerId);

        assertThat(reviews).hasSize(2);
        ReviewView first = reviews.get(0);
        ReviewView second = reviews.get(1);
        assertThat(first.status()).isEqualTo(ReviewStatus.LIFTED);
        assertThat(first.liftedAt()).isNotNull().isAfterOrEqualTo(first.placedAt());
        assertThat(second.status()).isEqualTo(ReviewStatus.OPEN);
        assertThat(second.liftedAt()).isNull();
        assertThat(second.placedAt()).isAfter(first.placedAt());
        assertThat(first.reviewId()).isNotEqualTo(second.reviewId());
    }

    @Test
    void a_read_of_a_customer_never_placed_lists_none() {
        assertThat(readReviews.reviews(freshCustomer())).isEmpty();
        assertThat(readReviews.reviews("")).isEmpty();
    }

    @Test
    void a_read_lists_nothing_of_another_customer() {
        String customerId = freshCustomer();
        String other = freshCustomer();
        placeReview.place(customerId);
        placeReview.place(other);

        assertThat(readReviews.reviews(customerId)).hasSize(1);
    }

    @Test
    void a_read_names_the_customer_as_the_same_text() {
        String customerId = "Customer-" + UUID.randomUUID();
        placeReview.place(customerId);

        assertThat(readReviews.reviews(customerId.toLowerCase())).isEmpty();
    }

    @Test
    void a_review_read_carries_no_customer_id_and_its_text_names_only_its_identifier() {
        String customerId = freshCustomer();
        placeReview.place(customerId);

        ReviewView review = readReviews.reviews(customerId).getFirst();

        assertThat(Arrays.stream(ReviewView.class.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly("reviewId", "status", "placedAt", "liftedAt");
        assertThat(review.toString()).doesNotContain(customerId).contains(review.reviewId().toString());
    }

    private static String freshCustomer() {
        return "customer-" + UUID.randomUUID();
    }
}
