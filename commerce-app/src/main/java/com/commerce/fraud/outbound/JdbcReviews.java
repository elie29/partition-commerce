package com.commerce.fraud.outbound;

import com.commerce.fraud.application.OpenReviewHeld;
import com.commerce.fraud.application.Reviews;
import com.commerce.fraud.domain.Review;
import com.commerce.fraud.domain.ReviewStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * The fraud reviews in the database, through {@link JdbcClient} (V11). The unique constraint on
 * {@code open_customer_id}, the {@code customer_id} while a review is open, holds BR-6 (P-8).
 */
@Repository
class JdbcReviews implements Reviews {

    private static final String COLUMNS = "review_id, customer_id, status, placed_at, lifted_at";

    private final JdbcClient jdbc;

    JdbcReviews(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void add(Review review) {
        try {
            jdbc.sql("INSERT INTO fraud_review (" + COLUMNS + ", open_customer_id)"
                            + " VALUES (:id, :customerId, :status, :placedAt, :liftedAt, :openCustomerId)")
                    .param("id", review.reviewId())
                    .param("customerId", review.customerId())
                    .param("status", review.status().name())
                    .param("placedAt", utc(review.placedAt()))
                    .param("liftedAt", utc(review.liftedAt()))
                    .param("openCustomerId", review.isOpen() ? review.customerId() : null)
                    .update();
        } catch (DuplicateKeyException e) {
            throw new OpenReviewHeld();
        }
    }

    @Override
    public Optional<Review> findOpen(String customerId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM fraud_review WHERE open_customer_id = :customerId")
                .param("customerId", customerId)
                .query(JdbcReviews::review)
                .optional();
    }

    @Override
    public void lift(Review lifted) {
        jdbc.sql("UPDATE fraud_review SET status = :status, lifted_at = :liftedAt, open_customer_id = NULL"
                        + " WHERE review_id = :id AND status = 'OPEN'")
                .param("status", lifted.status().name())
                .param("liftedAt", utc(lifted.liftedAt()))
                .param("id", lifted.reviewId())
                .update();
    }

    @Override
    public List<Review> ofCustomer(String customerId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM fraud_review WHERE customer_id = :customerId ORDER BY placed_at")
                .param("customerId", customerId)
                .query(JdbcReviews::review)
                .list();
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    private static Review review(ResultSet row, int rowNumber) throws SQLException {
        OffsetDateTime liftedAt = row.getObject("lifted_at", OffsetDateTime.class);
        return new Review(
                row.getObject("review_id", UUID.class),
                row.getString("customer_id"),
                ReviewStatus.valueOf(row.getString("status")),
                row.getObject("placed_at", OffsetDateTime.class).toInstant(),
                liftedAt == null ? null : liftedAt.toInstant());
    }
}
