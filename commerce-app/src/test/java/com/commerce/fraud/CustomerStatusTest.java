package com.commerce.fraud;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerce.fraud.application.CustomerStatus;
import com.commerce.fraud.application.LiftReviewUseCase;
import com.commerce.fraud.application.PlaceReviewUseCase;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * The use case behind {@code CustomerStatus}, the contract {@code fraud} shows {@code payment}: BR-3 and BR-4 of the
 * spec of {@code fraud}, in process and over HTTP (`docs/contracts/billing-internal.openapi.json`). A customer is
 * blocked while a review of it is open, not lifted.
 */
@FraudApiTest
class CustomerStatusTest {

    @LocalServerPort
    int port;

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

    @Test
    void a_customer_under_review_is_answered_blocked_over_http() {
        String customerId = freshCustomer();
        placeReview.place(customerId);

        HttpResponse<String> answer = status(customerId);

        assertThat(answer.statusCode()).isEqualTo(200);
        assertThat(answer.body()).isEqualToIgnoringWhitespace("{\"blocked\":true}");
    }

    @Test
    void a_customer_never_placed_is_answered_not_blocked_over_http() {
        HttpResponse<String> answer = status(freshCustomer());

        assertThat(answer.statusCode()).isEqualTo(200);
        assertThat(answer.body()).isEqualToIgnoringWhitespace("{\"blocked\":false}");
    }

    private HttpResponse<String> status(String customerId) {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port + "/internal/customers/" + customerId + "/status"))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static String freshCustomer() {
        return "customer-" + UUID.randomUUID();
    }
}
