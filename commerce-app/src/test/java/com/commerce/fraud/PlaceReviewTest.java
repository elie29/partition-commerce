package com.commerce.fraud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.commerce.fraud.application.LiftReviewUseCase;
import com.commerce.fraud.application.OpenReviewHeld;
import com.commerce.fraud.application.PlaceReviewUseCase;
import com.commerce.fraud.application.Reviews;
import com.commerce.fraud.domain.CustomerId;
import com.commerce.fraud.domain.InvalidCustomerId;
import com.commerce.fraud.domain.Review;
import com.commerce.fraud.domain.ReviewStatus;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * The use case "place a customer under fraud review": BR-5, BR-6, BR-7 and BR-9 of the spec of {@code fraud}, and the
 * {@code Review} aggregate as a place makes it (P-7). Only its tests reach the use case in this version.
 */
@FraudTest
@ExtendWith(OutputCaptureExtension.class)
class PlaceReviewTest {

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
    void a_place_records_one_open_review_with_its_own_identifier_and_its_time_placed_to_the_millisecond() {
        String customerId = freshCustomer();
        Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        placeReview.place(customerId);

        Instant after = Instant.now();
        assertThat(reviewCount(customerId)).isEqualTo(1);
        Review review = reviews.findOpen(customerId).orElseThrow();
        assertThat(review.reviewId()).isNotNull();
        assertThat(review.customerId()).isEqualTo(customerId);
        assertThat(review.status()).isEqualTo(ReviewStatus.OPEN);
        assertThat(review.liftedAt()).isNull();
        assertThat(review.placedAt()).isBetween(before, after);
        assertThat(review.placedAt()).isEqualTo(review.placedAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(storedPlacedAt(review.reviewId()).toInstant()).isEqualTo(review.placedAt());
    }

    @Test
    void two_customers_placed_get_two_reviews_with_distinct_identifiers() {
        String first = freshCustomer();
        String second = freshCustomer();

        placeReview.place(first);
        placeReview.place(second);

        assertThat(reviews.findOpen(first).orElseThrow().reviewId())
                .isNotEqualTo(reviews.findOpen(second).orElseThrow().reviewId());
    }

    @Test
    void placing_a_customer_already_under_review_changes_nothing() {
        String customerId = freshCustomer();
        placeReview.place(customerId);
        Review open = reviews.findOpen(customerId).orElseThrow();
        clearInvocations(reviews);

        placeReview.place(customerId);

        verify(reviews, never()).add(any());
        assertThat(reviewCount(customerId)).isEqualTo(1);
        assertThat(reviews.findOpen(customerId)).contains(open);
    }

    @Test
    void ten_places_of_one_customer_at_the_same_moment_leave_one_open_review_and_none_fails() throws Exception {
        String customerId = freshCustomer();
        int places = 10;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(places);
        try {
            List<Future<?>> done = new ArrayList<>();
            for (int i = 0; i < places; i++) {
                done.add(pool.submit(() -> {
                    start.await();
                    placeReview.place(customerId);
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> place : done) {
                place.get();
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(reviewCount(customerId)).isEqualTo(1);
        assertThat(openCount(customerId)).isEqualTo(1);
    }

    @Test
    void the_store_refuses_a_second_open_review_of_one_customer() {
        String customerId = freshCustomer();
        placeReview.place(customerId);

        assertThatThrownBy(() -> reviews.add(Review.place(UUID.randomUUID(), new CustomerId(customerId), Instant.now())))
                .isInstanceOf(OpenReviewHeld.class);
        assertThat(reviewCount(customerId)).isEqualTo(1);
    }

    @Test
    void a_place_after_a_lift_opens_a_new_review() {
        String customerId = freshCustomer();
        placeReview.place(customerId);
        UUID first = reviews.findOpen(customerId).orElseThrow().reviewId();
        liftReview.lift(customerId);

        placeReview.place(customerId);

        assertThat(reviewCount(customerId)).isEqualTo(2);
        assertThat(reviews.findOpen(customerId).orElseThrow().reviewId()).isNotEqualTo(first);
    }

    @Test
    void a_blank_customer_id_is_refused_and_nothing_is_placed() {
        for (String blank : List.of("", "   ", " ", "\t")) {
            clearInvocations(reviews);

            assertThatThrownBy(() -> placeReview.place(blank)).isInstanceOf(InvalidCustomerId.class);

            verify(reviews, never()).add(any());
            assertThat(reviewCount(blank)).isZero();
        }
    }

    @Test
    void a_missing_customer_id_is_refused_and_nothing_is_placed() {
        clearInvocations(reviews);

        assertThatThrownBy(() -> placeReview.place(null)).isInstanceOf(InvalidCustomerId.class);

        verify(reviews, never()).add(any());
    }

    @Test
    void a_customer_id_of_256_characters_is_refused_and_one_of_255_is_placed() {
        String longest = "😀".repeat(254) + UUID.randomUUID().toString().charAt(0);
        String tooLong = longest + "x";
        clearInvocations(reviews);

        assertThatThrownBy(() -> placeReview.place(tooLong)).isInstanceOf(InvalidCustomerId.class);
        verify(reviews, never()).add(any());
        assertThat(reviewCount(tooLong)).isZero();

        jdbc.sql("DELETE FROM fraud_review WHERE customer_id = :c").param("c", longest).update();
        placeReview.place(longest);
        assertThat(reviews.findOpen(longest)).isPresent();
    }

    @Test
    void a_customer_id_is_kept_as_the_same_text() {
        String customerId = "  Customer-" + UUID.randomUUID() + "  ";

        placeReview.place(customerId);

        assertThat(reviews.findOpen(customerId).orElseThrow().customerId()).isEqualTo(customerId);
        assertThat(reviews.findOpen(customerId.trim())).isEmpty();
    }

    @Test
    void no_output_and_no_error_names_the_customer_id(CapturedOutput output) {
        String customerId = freshCustomer();
        String tooLong = customerId + "x".repeat(256);

        placeReview.place(customerId);
        placeReview.place(customerId);
        Throwable refused = catchThrowable(() -> placeReview.place(tooLong));

        assertThat(refused).isInstanceOf(InvalidCustomerId.class);
        assertThat(refused.getMessage()).doesNotContain(customerId);
        assertThat(reviews.findOpen(customerId).orElseThrow().toString()).doesNotContain(customerId);
        assertThat(output.getAll()).doesNotContain(customerId);
    }

    @Test
    void a_review_is_placed_open_and_its_only_move_is_to_lifted() {
        Instant placedAt = Instant.parse("2026-10-20T10:15:30.123Z");
        Review review = Review.place(UUID.randomUUID(), new CustomerId("C-1"), placedAt);

        assertThat(review.status()).isEqualTo(ReviewStatus.OPEN);
        assertThat(review.isOpen()).isTrue();
        assertThat(review.placedAt()).isEqualTo(placedAt);
        assertThat(review.liftedAt()).isNull();
    }

    @Test
    void no_method_of_a_review_but_place_and_lift_makes_a_review_and_none_sets_its_status() {
        assertThat(Arrays.stream(Review.class.getDeclaredMethods())
                        .filter(m -> Modifier.isPublic(m.getModifiers()))
                        .filter(m -> m.getReturnType().equals(Review.class))
                        .map(Method::getName))
                .containsExactlyInAnyOrder("place", "lift");
        assertThat(Arrays.stream(Review.class.getDeclaredMethods()).map(Method::getName))
                .noneMatch(name -> name.startsWith("set") || name.startsWith("with") || name.equals("reopen"));
    }

    private static String freshCustomer() {
        return "customer-" + UUID.randomUUID();
    }

    private long reviewCount(String customerId) {
        return jdbc.sql("SELECT COUNT(*) FROM fraud_review WHERE customer_id = :c")
                .param("c", customerId)
                .query(Long.class)
                .single();
    }

    private long openCount(String customerId) {
        return jdbc.sql("SELECT COUNT(*) FROM fraud_review WHERE customer_id = :c AND status = 'OPEN'")
                .param("c", customerId)
                .query(Long.class)
                .single();
    }

    private OffsetDateTime storedPlacedAt(UUID reviewId) {
        return jdbc.sql("SELECT placed_at FROM fraud_review WHERE review_id = :id")
                .param("id", reviewId)
                .query(OffsetDateTime.class)
                .single();
    }
}
