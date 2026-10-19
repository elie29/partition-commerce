package com.commerce.refund;

import static com.commerce.refund.RefundClient.refundRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.commerce.refund.application.CardPayRefunds;
import com.commerce.refund.application.FraudReview;
import com.commerce.refund.application.Refunds;
import com.commerce.refund.domain.Money;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The use case "refund a paid invoice": BR-25 to BR-29, BR-31 and AR-3 of the spec of {@code refund}, the list of AR-3
 * included.
 */
@RefundApiTest
@ExtendWith(OutputCaptureExtension.class)
class RefundInvoiceTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    /** Reads what a refund recorded, and writes a payment as V4 copied it. */
    @Autowired
    JdbcClient jdbc;

    /** The fake, spied on: a test checks what is sent. */
    @MockitoSpyBean
    CardPayRefunds cardPay;

    /** Spied on: a test makes the record of {@code SENT} fail. */
    @MockitoSpyBean
    Refunds refunds;

    /** Spied on: a test puts the customer of a paid invoice under review, as a restart after an edit would. */
    @MockitoSpyBean
    FraudReview fraudReview;

    RefundClient client;

    @BeforeEach
    void client() {
        client = new RefundClient(port);
    }

    @Test
    void refunds_part_of_a_paid_invoice_sent_to_the_card_and_the_invoice_stays_paid() {
        String invoiceId = paid("customer-1", 1250);

        HttpResponse<String> refunded = client.refund(invoiceId, refundRequest(500, "EUR"));

        assertThat(refunded.statusCode()).isEqualTo(201);
        JsonNode refund = json.readTree(refunded.body());
        assertThat(refund.get("invoiceId").asString()).isEqualTo(invoiceId);
        assertThat(refund.get("amount").get("amountMinor").asLong()).isEqualTo(500);
        assertThat(refund.get("amount").get("currency").asString()).isEqualTo("EUR");
        assertThat(refund.get("status").asString()).isEqualTo("SENT");
        UUID refundId = UUID.fromString(refund.get("refundId").asString());
        verify(cardPay, times(1)).send(chargeReference(invoiceId), new Money(500, "EUR"));
        assertThat(recordedStatus(refundId)).isEqualTo("SENT");
        assertThat(status(invoiceId)).isEqualTo("PAID");
    }

    @Test
    void refunds_up_to_the_amount_paid_and_refuses_one_minor_unit_more_with_409() {
        String invoiceId = paid("customer-1", 1250);
        assertThat(client.refund(invoiceId, refundRequest(1000, "EUR")).statusCode()).isEqualTo(201);
        assertThat(client.refund(invoiceId, refundRequest(250, "EUR")).statusCode()).isEqualTo(201);

        HttpResponse<String> over = client.refund(invoiceId, refundRequest(1, "EUR"));

        assertThat(over.statusCode()).isEqualTo(409);
        assertThat(over.body()).isEmpty();
        verify(cardPay, times(2)).send(any(), any());
        assertThat(refundsOf(invoiceId)).isEqualTo(2);
    }

    @Test
    void refuses_more_than_the_amount_paid_in_one_refund_with_409() {
        String invoiceId = paid("customer-1", 1250);

        HttpResponse<String> over = client.refund(invoiceId, refundRequest(1251, "EUR"));

        assertThat(over.statusCode()).isEqualTo(409);
        assertThat(over.body()).isEmpty();
        verify(cardPay, never()).send(any(), any());
    }

    @Test
    void refuses_an_issued_or_a_cancelled_invoice_with_409_whatever_the_currency() {
        String issued = invoiceId(client.issue("customer-1", 1250, "EUR"));
        String cancelled = invoiceId(client.issue("customer-1", 1250, "EUR"));
        assertThat(client.cancel(cancelled).statusCode()).isEqualTo(200);

        for (String invoiceId : List.of(issued, cancelled)) {
            for (String currency : List.of("EUR", "USD")) {
                HttpResponse<String> response = client.refund(invoiceId, refundRequest(100, currency));
                assertThat(response.statusCode()).as(invoiceId + " " + currency).isEqualTo(409);
                assertThat(response.body()).isEmpty();
            }
        }
        verify(cardPay, never()).send(any(), any());
        assertThat(refundsOf(issued) + refundsOf(cancelled)).isZero();
    }

    @Test
    void answers_404_with_no_body_for_an_identifier_that_names_no_invoice_or_is_not_a_uuid() {
        for (String invoiceId : List.of(UUID.randomUUID().toString(), "42", "not-a-uuid", "1-1-1-1-1")) {
            HttpResponse<String> response = client.refund(invoiceId, refundRequest(100, "EUR"));
            assertThat(response.statusCode()).as(invoiceId).isEqualTo(404);
            assertThat(response.body()).as(invoiceId).isEmpty();
        }
        verify(cardPay, never()).send(any(), any());
    }

    @Test
    void refuses_a_content_the_contract_or_br_26_refuses_with_400_before_the_invoice_is_checked() {
        String paidId = paid("customer-1", 1250);
        List<String> refused = new ArrayList<>();
        refused.add(null);
        refused.addAll(List.of(
                "",
                "{not json",
                "{}",
                "{\"amount\":null}",
                "{\"amount\":{}}",
                "{\"amount\":{\"amountMinor\":100}}",
                "{\"amount\":{\"currency\":\"EUR\"}}",
                "{\"amount\":{\"amountMinor\":\"100\",\"currency\":\"EUR\"}}",
                "{\"amount\":{\"amountMinor\":1.5,\"currency\":\"EUR\"}}",
                "{\"amount\":{\"amountMinor\":100,\"currency\":\"eur\"}}",
                "{\"amount\":{\"amountMinor\":100,\"currency\":7}}",
                refundRequest(0, "EUR"),
                refundRequest(-1, "EUR")));

        for (String body : refused) {
            for (String invoiceId : List.of(paidId, UUID.randomUUID().toString(), "not-a-uuid")) {
                HttpResponse<String> response = client.refund(invoiceId, body);
                assertThat(response.statusCode()).as(body + " on " + invoiceId).isEqualTo(400);
                assertThat(response.body()).as(body + " on " + invoiceId).isEmpty();
            }
        }
        verify(cardPay, never()).send(any(), any());
        assertThat(refundsOf(paidId)).isZero();
    }

    @Test
    void refuses_a_currency_other_than_the_amount_paids_with_400() {
        String invoiceId = paid("customer-1", 1250);

        HttpResponse<String> response = client.refund(invoiceId, refundRequest(100, "USD"));

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).isEmpty();
        verify(cardPay, never()).send(any(), any());
        assertThat(refundsOf(invoiceId)).isZero();
    }

    @Test
    void refuses_a_customer_under_fraud_review_with_403_recording_nothing() {
        String invoiceId = paid("customer-reviewed-later", 1250);
        doReturn(true).when(fraudReview).underReview("customer-reviewed-later");

        for (String body : List.of(refundRequest(100, "EUR"), refundRequest(100, "USD"), refundRequest(5000, "EUR"))) {
            HttpResponse<String> response = client.refund(invoiceId, body);
            assertThat(response.statusCode()).as(body).isEqualTo(403);
            assertThat(response.body()).as(body).isEmpty();
        }
        verify(cardPay, never()).send(any(), any());
        assertThat(refundsOf(invoiceId)).isZero();
        assertThat(status(invoiceId)).isEqualTo("PAID");
    }

    @Test
    void reads_the_list_of_application_yaml_and_answers_400_and_404_before_403_and_403_before_409() {
        String underReview = invoiceId(client.issue(RefundApiTest.UNDER_REVIEW, 1250, "EUR"));
        assertThat(client.refund(underReview, refundRequest(0, "EUR")).statusCode()).isEqualTo(400);
        assertThat(client.refund(UUID.randomUUID().toString(), refundRequest(100, "EUR")).statusCode())
                .isEqualTo(404);

        HttpResponse<String> issued = client.refund(underReview, refundRequest(100, "EUR"));

        assertThat(issued.statusCode()).isEqualTo(403);
        verify(cardPay, never()).send(any(), any());
    }

    @Test
    void matches_a_customer_under_fraud_review_exactly() {
        for (String customerId : List.of("CUSTOMER-UNDER-REVIEW", RefundApiTest.UNDER_REVIEW + " ", "customer-under")) {
            HttpResponse<String> refund = client.refund(paid(customerId, 1250), refundRequest(100, "EUR"));
            assertThat(refund.statusCode()).as(customerId).isEqualTo(201);
        }
    }

    @Test
    void of_simultaneous_refunds_of_one_invoice_only_those_within_the_amount_paid_are_recorded() throws Exception {
        String invoiceId = paid("customer-1", 1250);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> refund = () -> {
            start.await();
            return client.refund(invoiceId, refundRequest(300, "EUR")).statusCode();
        };
        List<Integer> statuses = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            List<Future<Integer>> refunds = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                refunds.add(pool.submit(refund));
            }
            start.countDown();
            for (Future<Integer> f : refunds) {
                statuses.add(f.get());
            }
        }

        assertThat(statuses).filteredOn(s -> s == 201).hasSize(4);
        assertThat(statuses).filteredOn(s -> s == 409).hasSize(4);
        assertThat(refundsOf(invoiceId)).isEqualTo(4);
        assertThat(refunded(invoiceId)).isEqualTo(1200);
    }

    @Test
    void refunds_a_payment_copied_by_v4() {
        String invoiceId = invoiceId(client.issue("customer-1", 1250, "EUR"));
        jdbc.sql("UPDATE invoice SET status = 'PAID' WHERE invoice_id = ?").param(UUID.fromString(invoiceId)).update();
        jdbc.sql("INSERT INTO payment (payment_id, invoice_id, amount_minor, amount_currency, charge_reference, paid_at)"
                        + " VALUES (?, ?, 1250, 'EUR', 'ref-copied-by-v4', TIMESTAMP WITH TIME ZONE"
                        + " '2026-10-19 10:00:00+00')")
                .params(UUID.randomUUID(), UUID.fromString(invoiceId))
                .update();

        HttpResponse<String> refunded = client.refund(invoiceId, refundRequest(1250, "EUR"));

        assertThat(refunded.statusCode()).isEqualTo(201);
        verify(cardPay, times(1)).send("ref-copied-by-v4", new Money(1250, "EUR"));
    }

    @Test
    void answers_500_when_sent_is_not_recorded_and_the_refund_stays_requested_and_counts() {
        String invoiceId = paid("customer-1", 1250);
        doThrow(new DataAccessResourceFailureException("the database is down")).when(refunds).recordSent(any());

        HttpResponse<String> failed = client.refund(invoiceId, refundRequest(1000, "EUR"));

        assertThat(failed.statusCode()).isEqualTo(500);
        verify(cardPay, times(1)).send(any(), eq(new Money(1000, "EUR")));
        assertThat(jdbc.sql("SELECT status FROM refund WHERE invoice_id = ?")
                .param(UUID.fromString(invoiceId)).query(String.class).single()).isEqualTo("REQUESTED");
        assertThat(client.refund(invoiceId, refundRequest(251, "EUR")).statusCode()).isEqualTo(409);
    }

    @Test
    void no_output_names_the_charge_reference_the_amount_or_a_customer_under_review(CapturedOutput output) {
        String invoiceId = paid("customer-1", 987_654);
        String reference = chargeReference(invoiceId);
        List<HttpResponse<String>> answers = new ArrayList<>();
        answers.add(client.refund(invoiceId, refundRequest(876_543, "EUR")));
        answers.add(client.refund(invoiceId, refundRequest(876_543, "EUR")));
        answers.add(client.refund(invoiceId, refundRequest(876_543, "USD")));
        answers.add(client.refund(invoiceId, "{\"amount\":{\"amountMinor\":876543,\"currency\":\"EUR\"}"));
        answers.add(client.refund(invoiceId(client.issue(RefundApiTest.UNDER_REVIEW, 1250, "EUR")),
                refundRequest(876_543, "EUR")));
        doThrow(new DataAccessResourceFailureException("the database is down")).when(refunds).recordSent(any());
        answers.add(client.refund(invoiceId, refundRequest(1, "EUR")));

        assertThat(answers).extracting(HttpResponse::statusCode).containsExactly(201, 409, 400, 400, 403, 500);
        assertThat(answers.subList(1, 5)).extracting(HttpResponse::body).allMatch(String::isEmpty);
        assertThat(answers).extracting(HttpResponse::body)
                .noneMatch(body -> body.contains(reference) || body.contains(RefundApiTest.UNDER_REVIEW));
        assertThat(answers.get(5).body()).doesNotContain("876543", "\"amount\"");
        assertThat(output.getAll()).doesNotContain(reference, "876543", "876 543", RefundApiTest.UNDER_REVIEW);
    }

    /** An invoice issued for the customer, then paid through {@code payment}. */
    private String paid(String customerId, long amountMinor) {
        String invoiceId = invoiceId(client.issue(customerId, amountMinor, "EUR"));
        assertThat(client.pay(invoiceId).statusCode()).isEqualTo(200);
        return invoiceId;
    }

    private String invoiceId(HttpResponse<String> issued) {
        assertThat(issued.statusCode()).isEqualTo(201);
        return json.readTree(issued.body()).get("invoiceId").asString();
    }

    private String status(String invoiceId) {
        return json.readTree(client.read(invoiceId).body()).get("status").asString();
    }

    private String chargeReference(String invoiceId) {
        return jdbc.sql("SELECT charge_reference FROM payment WHERE invoice_id = ?")
                .param(UUID.fromString(invoiceId)).query(String.class).single();
    }

    private String recordedStatus(UUID refundId) {
        return jdbc.sql("SELECT status FROM refund WHERE refund_id = ?").param(refundId).query(String.class).single();
    }

    private int refundsOf(String invoiceId) {
        return jdbc.sql("SELECT COUNT(*) FROM refund WHERE invoice_id = ?")
                .param(UUID.fromString(invoiceId)).query(Integer.class).single();
    }

    private long refunded(String invoiceId) {
        return jdbc.sql("SELECT refunded_minor FROM refund_cap WHERE invoice_id = ?")
                .param(UUID.fromString(invoiceId)).query(Long.class).single();
    }
}
