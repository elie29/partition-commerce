package com.commerce.invoice;

import static com.commerce.invoice.InvoiceClient.line;
import static com.commerce.invoice.InvoiceClient.newOrderId;
import static com.commerce.invoice.InvoiceClient.request;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The use case "issue an invoice", a correction included: BR-1, BR-2, BR-7 to BR-14, BR-16 of the spec of {@code invoice}. */
@InvoiceApiTest
@ExtendWith(OutputCaptureExtension.class)
class IssueInvoiceTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    InvoiceClient client;

    @BeforeEach
    void client() {
        client = new InvoiceClient(port);
    }

    @Test
    void issues_the_invoice_with_its_own_identifier_and_the_sum_of_its_prices() {
        String orderId = newOrderId();

        HttpResponse<String> response = client.issue(request(orderId, "customer-1",
                line("Book", 1250, "EUR"), line("Pen", 199, "EUR"), line("Gift", 0, "EUR")));

        assertThat(response.statusCode()).isEqualTo(201);
        JsonNode invoice = json.readTree(response.body());
        assertThat(UUID.fromString(invoice.get("invoiceId").asString())).isNotNull();
        assertThat(invoice.get("orderId").asString()).isEqualTo(orderId);
        assertThat(invoice.get("status").asString()).isEqualTo("ISSUED");
        assertThat(invoice.get("amount").get("amountMinor").asLong()).isEqualTo(1449);
        assertThat(invoice.get("amount").get("currency").asString()).isEqualTo("EUR");
        assertThat(invoice.get("lines").valueStream().map(l -> l.get("label").asString()).toList())
                .containsExactly("Book", "Pen", "Gift");
        assertThat(invoice.has("customerId")).isFalse();
    }

    @Test
    void refuses_a_second_issue_for_the_same_order_with_409_and_no_body() {
        String orderId = newOrderId();
        assertThat(client.issue(request(orderId, "customer-1", line("Book", 1250, "EUR"))).statusCode())
                .isEqualTo(201);

        HttpResponse<String> second = client.issue(request(orderId, "customer-2", line("Pen", 199, "EUR")));

        assertThat(second.statusCode()).isEqualTo(409);
        assertThat(second.body()).isEmpty();
    }

    @Test
    void of_two_simultaneous_issues_of_one_order_the_second_is_refused() throws Exception {
        String body = request(newOrderId(), "customer-1", line("Book", 1250, "EUR"));
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> issue = () -> {
            start.await();
            return client.issue(body).statusCode();
        };
        List<Integer> statuses;
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Integer> first = pool.submit(issue);
            Future<Integer> second = pool.submit(issue);
            start.countDown();
            statuses = List.of(first.get(), second.get());
        }

        assertThat(statuses).containsExactlyInAnyOrder(201, 409);
    }

    @Test
    void issues_the_correction_of_a_cancelled_invoice_for_the_same_order_with_what_it_is_given() {
        String orderId = newOrderId();
        HttpResponse<String> cancelledIssue = client.issue(request(orderId, "customer-1", line("Book", 1250, "EUR")));
        String cancelledId = json.readTree(cancelledIssue.body()).get("invoiceId").asString();
        assertThat(client.cancel(cancelledId).statusCode()).isEqualTo(200);

        HttpResponse<String> correction = client.issue(request(orderId, "customer-2",
                line("Book", 1100, "USD"), line("Pen", 199, "USD")));

        assertThat(correction.statusCode()).isEqualTo(201);
        JsonNode invoice = json.readTree(correction.body());
        assertThat(invoice.get("invoiceId").asString()).isNotEqualTo(cancelledId);
        assertThat(invoice.get("orderId").asString()).isEqualTo(orderId);
        assertThat(invoice.get("status").asString()).isEqualTo("ISSUED");
        assertThat(invoice.get("amount").get("amountMinor").asLong()).isEqualTo(1299);
        assertThat(invoice.get("amount").get("currency").asString()).isEqualTo("USD");
        assertThat(json.readTree(client.read(cancelledId).body()).get("status").asString()).isEqualTo("CANCELLED");
    }

    @Test
    void refuses_an_issue_for_an_order_whose_correction_is_not_cancelled_with_409_and_no_body() {
        String orderId = newOrderId();
        HttpResponse<String> first = client.issue(request(orderId, "customer-1", line("Book", 1250, "EUR")));
        client.cancel(json.readTree(first.body()).get("invoiceId").asString());
        assertThat(client.issue(request(orderId, "customer-1", line("Book", 1100, "EUR"))).statusCode())
                .isEqualTo(201);

        HttpResponse<String> third = client.issue(request(orderId, "customer-1", line("Book", 1000, "EUR")));

        assertThat(third.statusCode()).isEqualTo(409);
        assertThat(third.body()).isEmpty();
    }

    @Test
    void keeps_the_framework_415_for_an_issue_with_no_content_type() {
        assertThat(client.postWithoutContentType("/invoices").statusCode()).isEqualTo(415);
    }

    @Test
    void refuses_an_issue_for_an_order_whose_invoice_is_paid_with_409_and_no_body() {
        String orderId = newOrderId();
        HttpResponse<String> first = client.issue(request(orderId, "customer-1", line("Book", 1250, "EUR")));
        String paidId = json.readTree(first.body()).get("invoiceId").asString();
        assertThat(client.recordPayment(paidId).statusCode()).isEqualTo(200);

        HttpResponse<String> second = client.issue(request(orderId, "customer-1", line("Book", 1250, "EUR")));

        assertThat(second.statusCode()).isEqualTo(409);
        assertThat(second.body()).isEmpty();
    }

    @Test
    void of_two_simultaneous_corrections_of_one_order_the_second_is_refused() throws Exception {
        String orderId = newOrderId();
        HttpResponse<String> first = client.issue(request(orderId, "customer-1", line("Book", 1250, "EUR")));
        client.cancel(json.readTree(first.body()).get("invoiceId").asString());
        String body = request(orderId, "customer-1", line("Book", 1100, "EUR"));
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> issue = () -> {
            start.await();
            return client.issue(body).statusCode();
        };
        List<Integer> statuses;
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Integer> one = pool.submit(issue);
            Future<Integer> two = pool.submit(issue);
            start.countDown();
            statuses = List.of(one.get(), two.get());
        }

        assertThat(statuses).containsExactlyInAnyOrder(201, 409);
    }

    @Test
    void refuses_lines_in_different_currencies() {
        assertRefused(request(newOrderId(), "customer-1", line("Book", 1250, "EUR"), line("Pen", 199, "USD")));
    }

    @Test
    void refuses_a_negative_price_and_accepts_a_price_of_zero() {
        assertRefused(request(newOrderId(), "customer-1", line("Book", -1, "EUR")));
        assertThat(client.issue(request(newOrderId(), "customer-1", line("Gift", 0, "EUR"))).statusCode())
                .isEqualTo(201);
    }

    @Test
    void refuses_a_currency_that_is_not_a_known_iso_4217_code() {
        assertRefused(request(newOrderId(), "customer-1", line("Book", 1250, "ABC")));
        assertRefused(request(newOrderId(), "customer-1", line("Book", 1250, "eur")));
    }

    @Test
    void refuses_a_currency_starting_with_x() {
        assertRefused(request(newOrderId(), "customer-1", line("Book", 1250, "XXX")));
        assertRefused(request(newOrderId(), "customer-1", line("Book", 1250, "XTS")));
        assertRefused(request(newOrderId(), "customer-1", line("Book", 1250, "XAU")));
        assertRefused(request(newOrderId(), "customer-1", line("Book", 1250, "XDR")));
    }

    @Test
    void accepts_a_fund_code_and_a_withdrawn_code_the_jdk_lists() {
        assertThat(client.issue(request(newOrderId(), "customer-1", line("Book", 1250, "BOV"))).statusCode())
                .isEqualTo(201);
        assertThat(client.issue(request(newOrderId(), "customer-1", line("Book", 1250, "DEM"))).statusCode())
                .isEqualTo(201);
    }

    @Test
    void refuses_a_sum_of_prices_beyond_the_range_of_the_amount() {
        assertRefused(request(newOrderId(), "customer-1",
                line("Book", Long.MAX_VALUE, "EUR"), line("Pen", 1, "EUR")));
    }

    @Test
    void refuses_an_order_or_customer_identifier_blank_or_over_255_characters() {
        String longest = "x".repeat(255);
        assertRefused(request("", "customer-1", line("Book", 1250, "EUR")));
        assertRefused(request(" \t", "customer-1", line("Book", 1250, "EUR")));
        assertRefused(request(longest + "x", "customer-1", line("Book", 1250, "EUR")));
        assertRefused(request(newOrderId(), "  ", line("Book", 1250, "EUR")));
        assertRefused(request(newOrderId(), longest + "x", line("Book", 1250, "EUR")));
        assertThat(client.issue(request("o".repeat(255), longest, line("Book", 1250, "EUR"))).statusCode())
                .isEqualTo(201);
        assertThat(client.issue(request("😀".repeat(255), "😀".repeat(255), line("Book", 1250, "EUR"))).statusCode())
                .isEqualTo(201);
    }

    @Test
    void refuses_a_label_blank_or_over_255_characters() {
        assertRefused(request(newOrderId(), "customer-1", line(" ", 1250, "EUR")));
        assertRefused(request(newOrderId(), "customer-1", line("x".repeat(256), 1250, "EUR")));
        assertThat(client.issue(request(newOrderId(), "customer-1", line("😀".repeat(255), 1250, "EUR")))
                .statusCode()).isEqualTo(201);
    }

    @Test
    void refuses_a_text_of_only_non_breaking_spaces_as_blank() {
        String nonBreaking = "   ";
        assertRefused(request(nonBreaking, "customer-1", line("Book", 1250, "EUR")));
        assertRefused(request(newOrderId(), nonBreaking, line("Book", 1250, "EUR")));
        assertRefused(request(newOrderId(), "customer-1", line(nonBreaking, 1250, "EUR")));
    }

    @Test
    void refuses_what_the_contract_refuses() {
        String orderId = newOrderId();
        assertRefused(request(orderId, "customer-1"));
        assertRefused(request(null, "customer-1", line("Book", 1250, "EUR")));
        assertRefused(request(orderId, null, line("Book", 1250, "EUR")));
        assertRefused(request(orderId, "customer-1", line(null, 1250, "EUR")));
        assertRefused(request(orderId, "customer-1", "null"));
        assertRefused(request(orderId, "customer-1", "{\"label\":\"Book\"}"));
        assertRefused(request(orderId, "customer-1", "{\"label\":\"Book\",\"price\":{\"amountMinor\":1.5,\"currency\":\"EUR\"}}"));
        assertRefused(request(orderId, "customer-1", "{\"label\":\"Book\",\"price\":{\"amountMinor\":\"100\",\"currency\":\"EUR\"}}"));
        assertRefused(request(orderId, "customer-1", "{\"label\":\"Book\",\"price\":{\"amountMinor\":9223372036854775808,\"currency\":\"EUR\"}}"));
        assertRefused("{\"orderId\":42,\"customerId\":\"customer-1\",\"lines\":[" + line("Book", 1250, "EUR") + "]}");
        assertRefused("{\"orderId\":");
    }

    @Test
    void refuses_a_content_before_looking_for_an_invoice_of_the_order() {
        String orderId = newOrderId();
        assertThat(client.issue(request(orderId, "customer-1", line("Book", 1250, "EUR"))).statusCode())
                .isEqualTo(201);

        assertRefused(request(orderId, "customer-1", line("Book", -1, "EUR")));
    }

    @Test
    void logs_the_identifiers_only(CapturedOutput output) {
        String orderId = newOrderId();

        HttpResponse<String> response = client.issue(request(orderId, "customer-1", line("Secret label", 4242, "EUR")));

        String invoiceId = json.readTree(response.body()).get("invoiceId").asString();
        assertThat(output.getAll()).contains(invoiceId).contains(orderId)
                .doesNotContain("Secret label").doesNotContain("4242");
    }

    private void assertRefused(String body) {
        HttpResponse<String> response = client.issue(body);
        assertThat(response.statusCode()).as(body).isEqualTo(400);
        assertThat(response.body()).as(body).isEmpty();
    }
}
