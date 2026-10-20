package com.commerce.fraud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.commerce.fraud.application.LiftReviewUseCase;
import com.commerce.fraud.application.PlaceReviewUseCase;
import com.commerce.fraud.application.Reviews;
import com.commerce.fraud.domain.CustomerId;
import com.commerce.fraud.domain.Review;
import com.commerce.fraud.domain.ReviewStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * The use case "lift a customer's fraud review": BR-1 and BR-9 of the spec of {@code fraud}, and the move of the
 * {@code Review} aggregate from open to lifted (P-7). Only its tests reach the use case in this version.
 */
@FraudTest
@ExtendWith(OutputCaptureExtension.class)
class LiftReviewTest {

    @Autowired
    PlaceReviewUseCase placeReview;

    @Autowired
    LiftReviewUseCase liftReview;

    @Autowired
    JdbcClient jdbc;

    /** Spied on: a test checks what the use case writes. */
    @MockitoSpyBean
    Reviews reviews;

    @Test
    void a_lift_lifts_the_customers_open_review_with_its_time_lifted_to_the_millisecond() {
        String customerId = freshCustomer();
        placeReview.place(customerId);
        UUID reviewId = reviews.findOpen(customerId).orElseThrow().reviewId();
        Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        liftReview.lift(customerId);

        Instant after = Instant.now();
        assertThat(reviews.findOpen(customerId)).isEmpty();
        Review lifted = reviews.ofCustomer(customerId).getFirst();
        assertThat(lifted.reviewId()).isEqualTo(reviewId);
        assertThat(lifted.status()).isEqualTo(ReviewStatus.LIFTED);
        assertThat(lifted.liftedAt()).isBetween(before, after);
        assertThat(lifted.liftedAt()).isEqualTo(lifted.liftedAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(statusOf(reviewId)).isEqualTo("LIFTED");
    }

    @Test
    void a_lift_of_a_customer_with_no_open_review_changes_nothing() {
        String never = freshCustomer();
        clearInvocations(reviews);

        liftReview.lift(never);

        verify(reviews, never()).lift(any());
        assertThat(reviews.ofCustomer(never)).isEmpty();
    }

    @Test
    void a_second_lift_changes_nothing() {
        String customerId = freshCustomer();
        placeReview.place(customerId);
        liftReview.lift(customerId);
        Review lifted = reviews.ofCustomer(customerId).getFirst();
        clearInvocations(reviews);

        liftReview.lift(customerId);

        verify(reviews, never()).lift(any());
        assertThat(reviews.ofCustomer(customerId)).containsExactly(lifted);
    }

    @Test
    void a_lift_names_the_customer_as_the_same_text_and_a_text_no_review_names_lifts_nothing() {
        String customerId = "Customer-" + UUID.randomUUID();
        placeReview.place(customerId);
        clearInvocations(reviews);

        liftReview.lift(customerId.toLowerCase());
        liftReview.lift("");
        liftReview.lift(customerId + "x".repeat(300));

        verify(reviews, never()).lift(any());
        assertThat(reviews.findOpen(customerId)).isPresent();
    }

    @Test
    void a_lift_leaves_the_other_customers_reviews_open() {
        String lifted = freshCustomer();
        String other = freshCustomer();
        placeReview.place(lifted);
        placeReview.place(other);

        liftReview.lift(lifted);

        assertThat(reviews.findOpen(other)).isPresent();
    }

    @Test
    void the_lift_logs_no_customer_id(CapturedOutput output) {
        String customerId = freshCustomer();
        placeReview.place(customerId);

        liftReview.lift(customerId);
        liftReview.lift(customerId);

        assertThat(output.getAll()).doesNotContain(customerId);
    }

    @Test
    void a_lifted_review_is_never_open_again_and_a_second_lift_leaves_it_as_it_is() {
        Instant placedAt = Instant.parse("2026-10-20T10:15:30.123Z");
        Instant liftedAt = Instant.parse("2026-10-20T11:00:00.456Z");
        Review open = Review.place(UUID.randomUUID(), new CustomerId("C-1"), placedAt);

        Review lifted = open.lift(liftedAt);

        assertThat(lifted.status()).isEqualTo(ReviewStatus.LIFTED);
        assertThat(lifted.isOpen()).isFalse();
        assertThat(lifted.liftedAt()).isEqualTo(liftedAt);
        assertThat(lifted.placedAt()).isEqualTo(placedAt);
        assertThat(lifted.reviewId()).isEqualTo(open.reviewId());
        assertThat(lifted.lift(liftedAt.plusSeconds(60))).isEqualTo(lifted);
    }

    private static String freshCustomer() {
        return "customer-" + UUID.randomUUID();
    }

    private String statusOf(UUID reviewId) {
        return jdbc.sql("SELECT status FROM fraud_review WHERE review_id = :id")
                .param("id", reviewId)
                .query(String.class)
                .single();
    }
}
