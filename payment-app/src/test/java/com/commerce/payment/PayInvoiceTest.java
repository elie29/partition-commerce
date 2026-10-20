package com.commerce.payment;

import static com.commerce.payment.PaymentClient.line;
import static com.commerce.payment.PaymentClient.newOrderId;
import static com.commerce.payment.PaymentClient.payRequest;
import static com.commerce.payment.PaymentClient.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.commerce.payment.application.CardPay;
import com.commerce.payment.application.Charge;
import com.commerce.payment.application.Payments;
import com.commerce.payment.domain.Money;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The use case "pay an invoice by card": BR-5, BR-20, BR-21, BR-22, BR-24 and BR-32 of the spec of {@code payment},
 * {@code commerce-app} faked by {@link CommerceAppStub}: the invoice, and the customer under fraud review placed and
 * lifted there, which {@code payment} asks through {@code CustomerStatus}.
 */
@PaymentApiTest
@ExtendWith(OutputCaptureExtension.class)
class PayInvoiceTest {

    private static final String TOKEN = "tok_visa_4242";

    private static final CommerceAppStub COMMERCE = CommerceAppStub.INSTANCE;

    @DynamicPropertySource
    static void commerceApp(DynamicPropertyRegistry registry) {
        registry.add("commerce.billing-internal.base-url", COMMERCE::baseUrl);
    }

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    /** The fake, spied on: a test makes CardPay decline, or answer out of bounds. */
    @MockitoSpyBean
    CardPay cardPay;

    /** Spied on: a test makes the store of the payment fail, once {@code invoice} recorded it. */
    @MockitoSpyBean
    Payments payments;

    PaymentClient client;

    @BeforeEach
    void client() {
        client = new PaymentClient(port);
        COMMERCE.place(PaymentApiTest.UNDER_REVIEW);
    }

    @AfterEach
    void commerceAnswersAgain() {
        COMMERCE.reset();
    }

    @Test
    void pays_an_issued_invoice_changing_its_status_only() {
        HttpResponse<String> issued = issue(line("Book", 1250, "EUR"), line("Pen", 199, "EUR"));

        HttpResponse<String> paid = client.pay(invoiceId(issued), payRequest(TOKEN));

        assertThat(paid.statusCode()).isEqualTo(200);
        JsonNode invoice = json.readTree(paid.body());
        assertThat(invoice.get("status").asString()).isEqualTo("PAID");
        assertThat(withoutStatus(invoice)).isEqualTo(withoutStatus(json.readTree(issued.body())));
        assertThat(invoice.has("customerId")).isFalse();
        verify(cardPay, times(1)).charge(any(), eq(new Money(1449, "EUR")));
    }

    @Test
    void reads_a_paid_invoice_back_paid() {
        String invoiceId = invoiceId(issue());
        HttpResponse<String> paid = client.pay(invoiceId, payRequest(TOKEN));

        HttpResponse<String> read = client.read(invoiceId);

        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(json.readTree(read.body())).isEqualTo(json.readTree(paid.body()));
    }

    @Test
    void charges_an_invoice_whose_amount_is_zero_like_any_other() {
        String invoiceId = invoiceId(issue(line("Gift", 0, "EUR")));

        HttpResponse<String> paid = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(paid.statusCode()).isEqualTo(200);
        verify(cardPay, times(1)).charge(any(), eq(new Money(0, "EUR")));
    }

    @Test
    void answers_402_with_no_body_when_cardpay_declines_and_the_invoice_stays_issued() {
        doReturn(new Charge.Declined()).when(cardPay).charge(any(), any());
        String invoiceId = invoiceId(issue());

        HttpResponse<String> declined = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(declined.statusCode()).isEqualTo(402);
        assertThat(declined.body()).isEmpty();
        assertThat(status(invoiceId)).isEqualTo("ISSUED");
        assertThat(client.cancel(invoiceId).statusCode()).isEqualTo(200);
    }

    @Test
    void refuses_to_pay_a_paid_invoice_with_409_without_calling_cardpay() {
        String invoiceId = invoiceId(issue());
        assertThat(client.pay(invoiceId, payRequest(TOKEN)).statusCode()).isEqualTo(200);

        HttpResponse<String> second = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(second.statusCode()).isEqualTo(409);
        assertThat(second.body()).isEmpty();
        verify(cardPay, times(1)).charge(any(), any());
        assertThat(status(invoiceId)).isEqualTo("PAID");
    }

    @Test
    void refuses_to_pay_a_cancelled_invoice_with_409_without_calling_cardpay() {
        String invoiceId = invoiceId(issue());
        assertThat(client.cancel(invoiceId).statusCode()).isEqualTo(200);

        HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(pay.statusCode()).isEqualTo(409);
        assertThat(pay.body()).isEmpty();
        verify(cardPay, never()).charge(any(), any());
        assertThat(status(invoiceId)).isEqualTo("CANCELLED");
    }

    @Test
    void of_two_simultaneous_pays_of_one_invoice_the_second_is_refused() throws Exception {
        String invoiceId = invoiceId(issue());
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> pay = () -> {
            start.await();
            return client.pay(invoiceId, payRequest(TOKEN)).statusCode();
        };
        List<Integer> statuses;
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Integer> first = pool.submit(pay);
            Future<Integer> second = pool.submit(pay);
            start.countDown();
            statuses = List.of(first.get(), second.get());
        }

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        assertThat(status(invoiceId)).isEqualTo("PAID");
    }

    @Test
    void answers_404_with_no_body_for_an_identifier_that_names_no_invoice_or_is_not_a_uuid() {
        for (String invoiceId : List.of(UUID.randomUUID().toString(), "42", "not-a-uuid", "1-1-1-1-1")) {
            HttpResponse<String> response = client.pay(invoiceId, payRequest(TOKEN));
            assertThat(response.statusCode()).as(invoiceId).isEqualTo(404);
            assertThat(response.body()).as(invoiceId).isEmpty();
        }
        verify(cardPay, never()).charge(any(), any());
    }

    @Test
    void refuses_a_content_the_contract_or_br_22_refuses_with_400_before_the_invoice_is_checked() {
        String issuedId = invoiceId(issue());
        List<String> refused = new ArrayList<>();
        refused.add(null);
        refused.addAll(List.of(
                "",
                "{not json",
                "{}",
                "{\"cardToken\":null}",
                "{\"cardToken\":42}",
                payRequest(""),
                payRequest(" \t "),
                payRequest(" "),
                payRequest("t".repeat(256))));

        for (String body : refused) {
            for (String invoiceId : List.of(issuedId, UUID.randomUUID().toString(), "not-a-uuid")) {
                HttpResponse<String> response = client.pay(invoiceId, body);
                assertThat(response.statusCode()).as(body + " on " + invoiceId).isEqualTo(400);
                assertThat(response.body()).as(body + " on " + invoiceId).isEmpty();
            }
        }
        verify(cardPay, never()).charge(any(), any());
        assertThat(status(issuedId)).isEqualTo("ISSUED");
    }

    @Test
    void keeps_the_framework_415_for_a_pay_with_no_content_type() {
        String invoiceId = invoiceId(issue());

        HttpResponse<String> response = client.postWithoutContentType("/invoices/" + invoiceId + "/pay");

        assertThat(response.statusCode()).isEqualTo(415);
        verify(cardPay, never()).charge(any(), any());
        assertThat(status(invoiceId)).isEqualTo("ISSUED");
    }

    @Test
    void accepts_a_card_token_of_255_characters_counted_as_code_points() {
        String invoiceId = invoiceId(issue());

        HttpResponse<String> paid = client.pay(invoiceId, payRequest("😀".repeat(255)));

        assertThat(paid.statusCode()).isEqualTo(200);
    }

    @Test
    void answers_500_and_the_invoice_stays_as_invoice_recorded_it_when_the_store_fails() {
        doThrow(new DataAccessResourceFailureException("the database is down"))
                .when(payments).add(any());
        String invoiceId = invoiceId(issue());

        HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(pay.statusCode()).isEqualTo(500);
        assertThat(status(invoiceId)).isEqualTo("PAID");
    }

    @Test
    void answers_500_and_the_invoice_stays_issued_when_cardpay_answers_a_charge_reference_out_of_bounds() {
        doReturn(new Charge.Accepted("r".repeat(256))).when(cardPay).charge(any(), any());
        String invoiceId = invoiceId(issue());

        HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(pay.statusCode()).isEqualTo(500);
        assertThat(status(invoiceId)).isEqualTo("ISSUED");
    }

    @Test
    void the_card_token_appears_in_no_log_and_no_answer(CapturedOutput output) {
        String token = "tok_" + UUID.randomUUID();
        List<HttpResponse<String>> answers = new ArrayList<>();
        String paidId = invoiceId(issue());
        answers.add(client.pay(paidId, payRequest(token)));
        answers.add(client.pay(paidId, payRequest(token)));
        answers.add(client.pay(UUID.randomUUID().toString(), payRequest(token)));
        answers.add(client.pay(paidId, "{\"cardToken\":\"" + token + "\""));
        answers.add(client.pay(paidId, "{\"cardToken\":\"" + token + "\",\"cardToken\":7}"));
        doReturn(new Charge.Declined()).when(cardPay).charge(any(), any());
        answers.add(client.pay(invoiceId(issue()), payRequest(token)));
        doReturn(new Charge.Accepted("r".repeat(256))).when(cardPay).charge(any(), any());
        answers.add(client.pay(invoiceId(issue()), payRequest(token)));
        doReturn(new Charge.Accepted("ref-1")).when(cardPay).charge(any(), any());
        doThrow(new DataAccessResourceFailureException("the database is down"))
                .when(payments).add(any());
        answers.add(client.pay(invoiceId(issue()), payRequest(token)));

        assertThat(answers).extracting(HttpResponse::statusCode).containsExactly(200, 409, 404, 400, 400, 402, 500, 500);
        assertThat(answers).extracting(HttpResponse::body).noneMatch(body -> body.contains(token));
        assertThat(output.getAll()).doesNotContain(token);
    }

    @Test
    void refuses_to_pay_for_a_customer_under_fraud_review_with_403_without_calling_cardpay() {
        String invoiceId = invoiceId(issueFor(PaymentApiTest.UNDER_REVIEW));

        HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(pay.statusCode()).isEqualTo(403);
        assertThat(pay.body()).isEmpty();
        verify(cardPay, never()).charge(any(), any());
        assertThat(status(invoiceId)).isEqualTo("ISSUED");
    }

    @Test
    void matches_a_customer_under_fraud_review_exactly() {
        for (String customerId : List.of("CUSTOMER-UNDER-REVIEW", PaymentApiTest.UNDER_REVIEW + " ", "customer-under")) {
            HttpResponse<String> pay = client.pay(invoiceId(issueFor(customerId)), payRequest(TOKEN));
            assertThat(pay.statusCode()).as(customerId).isEqualTo(200);
        }
    }

    @Test
    void answers_400_and_404_before_403_and_403_before_409() {
        String underReview = invoiceId(issueFor(PaymentApiTest.UNDER_REVIEW));
        assertThat(client.pay(underReview, "{}").statusCode()).isEqualTo(400);
        assertThat(client.pay(underReview, payRequest("")).statusCode()).isEqualTo(400);
        assertThat(client.pay(UUID.randomUUID().toString(), payRequest(TOKEN)).statusCode()).isEqualTo(404);

        assertThat(client.cancel(underReview).statusCode()).isEqualTo(200);
        HttpResponse<String> cancelled = client.pay(underReview, payRequest(TOKEN));

        assertThat(cancelled.statusCode()).isEqualTo(403);
        verify(cardPay, never()).charge(any(), any());
    }

    @Test
    void no_output_names_a_customer_under_fraud_review(CapturedOutput output) {
        String invoiceId = invoiceId(issueFor(PaymentApiTest.UNDER_REVIEW));

        HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(pay.statusCode()).isEqualTo(403);
        assertThat(output.getAll()).doesNotContain(PaymentApiTest.UNDER_REVIEW);
    }

    @Test
    void no_output_names_a_customer_under_fraud_review_when_customer_status_fails(CapturedOutput output) {
        for (Runnable failure : List.<Runnable>of(
                () -> COMMERCE.slow("getCustomerStatus"), () -> COMMERCE.failing("getCustomerStatus"))) {
            COMMERCE.reset();
            failure.run();

            HttpResponse<String> pay = client.pay(invoiceId(issueFor(PaymentApiTest.UNDER_REVIEW)), payRequest(TOKEN));

            assertThat(pay.statusCode()).isEqualTo(500);
            assertThat(pay.body()).doesNotContain(PaymentApiTest.UNDER_REVIEW);
        }
        assertThat(output.getAll()).doesNotContain(PaymentApiTest.UNDER_REVIEW);
    }

    @Test
    void pays_once_the_review_of_its_customer_is_lifted() {
        String customerId = "customer-" + UUID.randomUUID();
        COMMERCE.place(customerId);
        String invoiceId = invoiceId(issueFor(customerId));
        assertThat(client.pay(invoiceId, payRequest(TOKEN)).statusCode()).isEqualTo(403);

        COMMERCE.lift(customerId);
        HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(pay.statusCode()).isEqualTo(200);
        assertThat(status(invoiceId)).isEqualTo("PAID");
    }

    @Test
    void answers_500_without_calling_cardpay_or_recording_when_customer_status_cannot_answer() {
        COMMERCE.failing("getCustomerStatus");
        String invoiceId = invoiceId(issue());

        HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(pay.statusCode()).isEqualTo(500);
        verify(cardPay, never()).charge(any(), any());
        verify(payments, never()).add(any());
        assertThat(status(invoiceId)).isEqualTo("ISSUED");
    }

    @Test
    void answers_500_without_calling_cardpay_when_a_read_before_the_charge_takes_more_than_2_seconds() {
        for (String operation : List.of("getInvoiceForPayment", "getInvoiceCustomer", "getCustomerStatus")) {
            COMMERCE.reset();
            COMMERCE.slow(operation);
            String invoiceId = invoiceId(issue());

            HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

            assertThat(pay.statusCode()).as(operation).isEqualTo(500);
            COMMERCE.reset();
            assertThat(status(invoiceId)).as(operation).isEqualTo("ISSUED");
        }
        verify(cardPay, never()).charge(any(), any());
        verify(payments, never()).add(any());
    }

    @Test
    void answers_500_storing_nothing_when_the_record_of_invoice_takes_more_than_2_seconds() {
        COMMERCE.slow("recordPayment");
        String invoiceId = invoiceId(issue());

        HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(pay.statusCode()).isEqualTo(500);
        verify(cardPay, times(1)).charge(any(), any());
        verify(payments, never()).add(any());
    }

    @Test
    void answers_500_without_calling_cardpay_when_commerce_app_is_unreachable() {
        String invoiceId = invoiceId(issue());
        COMMERCE.unreachable();

        HttpResponse<String> pay = client.pay(invoiceId, payRequest(TOKEN));

        assertThat(pay.statusCode()).isEqualTo(500);
        verify(cardPay, never()).charge(any(), any());
        verify(payments, never()).add(any());
        COMMERCE.reset();
        assertThat(status(invoiceId)).isEqualTo("ISSUED");
    }

    private HttpResponse<String> issue(String... lines) {
        String[] issuedLines = lines.length == 0 ? new String[] {line("Book", 1250, "EUR")} : lines;
        HttpResponse<String> issued = client.issue(request(newOrderId(), "customer-1", issuedLines));
        assertThat(issued.statusCode()).isEqualTo(201);
        return issued;
    }

    private HttpResponse<String> issueFor(String customerId) {
        HttpResponse<String> issued = client.issue(request(newOrderId(), customerId, line("Book", 1250, "EUR")));
        assertThat(issued.statusCode()).isEqualTo(201);
        return issued;
    }

    private String invoiceId(HttpResponse<String> response) {
        return json.readTree(response.body()).get("invoiceId").asString();
    }

    private String status(String invoiceId) {
        return json.readTree(client.read(invoiceId).body()).get("status").asString();
    }

    private static JsonNode withoutStatus(JsonNode invoice) {
        ObjectNode copy = (ObjectNode) invoice.deepCopy();
        copy.remove("status");
        return copy;
    }
}
