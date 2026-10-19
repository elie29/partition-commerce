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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** The use case "cancel an invoice": BR-3, BR-4, BR-18, BR-19 of the spec of {@code invoice}. */
@InvoiceApiTest
class CancelInvoiceTest {

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
    void cancels_an_issued_invoice_changing_its_status_only() {
        HttpResponse<String> issued = issue();

        HttpResponse<String> cancelled = client.cancel(invoiceId(issued));

        assertThat(cancelled.statusCode()).isEqualTo(200);
        JsonNode invoice = json.readTree(cancelled.body());
        assertThat(invoice.get("status").asString()).isEqualTo("CANCELLED");
        assertThat(withoutStatus(invoice)).isEqualTo(withoutStatus(json.readTree(issued.body())));
        assertThat(invoice.has("customerId")).isFalse();
    }

    @Test
    void reads_a_cancelled_invoice_back_cancelled_as_it_was_issued() {
        HttpResponse<String> issued = issue();
        HttpResponse<String> cancelled = client.cancel(invoiceId(issued));

        HttpResponse<String> read = client.read(invoiceId(issued));

        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(json.readTree(read.body())).isEqualTo(json.readTree(cancelled.body()));
    }

    @Test
    void refuses_to_cancel_a_cancelled_invoice_with_409_and_no_body() {
        String invoiceId = invoiceId(issue());
        assertThat(client.cancel(invoiceId).statusCode()).isEqualTo(200);

        HttpResponse<String> second = client.cancel(invoiceId);

        assertThat(second.statusCode()).isEqualTo(409);
        assertThat(second.body()).isEmpty();
        assertThat(json.readTree(client.read(invoiceId).body()).get("status").asString()).isEqualTo("CANCELLED");
    }

    @Test
    void refuses_to_cancel_a_paid_invoice_with_409_and_no_body() {
        String invoiceId = invoiceId(issue());
        assertThat(client.pay(invoiceId, InvoiceClient.payRequest("tok_visa")).statusCode()).isEqualTo(200);

        HttpResponse<String> cancel = client.cancel(invoiceId);

        assertThat(cancel.statusCode()).isEqualTo(409);
        assertThat(cancel.body()).isEmpty();
        assertThat(json.readTree(client.read(invoiceId).body()).get("status").asString()).isEqualTo("PAID");
    }

    @Test
    void of_a_simultaneous_pay_and_cancel_of_one_invoice_the_second_is_refused() throws Exception {
        String invoiceId = invoiceId(issue());
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> cancel = () -> {
            start.await();
            return client.cancel(invoiceId).statusCode();
        };
        Callable<Integer> pay = () -> {
            start.await();
            return client.pay(invoiceId, InvoiceClient.payRequest("tok_visa")).statusCode();
        };
        List<Integer> statuses;
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Integer> cancelled = pool.submit(cancel);
            Future<Integer> paid = pool.submit(pay);
            start.countDown();
            statuses = List.of(cancelled.get(), paid.get());
        }

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        String status = json.readTree(client.read(invoiceId).body()).get("status").asString();
        assertThat(status).isEqualTo(statuses.get(0) == 200 ? "CANCELLED" : "PAID");
    }

    @Test
    void of_two_simultaneous_cancels_of_one_invoice_the_second_is_refused() throws Exception {
        String invoiceId = invoiceId(issue());
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> cancel = () -> {
            start.await();
            return client.cancel(invoiceId).statusCode();
        };
        List<Integer> statuses;
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Integer> first = pool.submit(cancel);
            Future<Integer> second = pool.submit(cancel);
            start.countDown();
            statuses = List.of(first.get(), second.get());
        }

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
    }

    @Test
    void answers_404_with_no_body_for_an_identifier_that_names_no_invoice() {
        assertNotFound(UUID.randomUUID().toString());
    }

    @Test
    void answers_404_with_no_body_for_an_identifier_that_is_not_a_uuid() {
        assertNotFound("42");
        assertNotFound("not-a-uuid");
        assertNotFound("1-1-1-1-1");
    }

    private HttpResponse<String> issue() {
        HttpResponse<String> issued = client.issue(request(newOrderId(), "customer-1",
                line("Book", 1250, "EUR"), line("Pen", 199, "EUR")));
        assertThat(issued.statusCode()).isEqualTo(201);
        return issued;
    }

    private String invoiceId(HttpResponse<String> response) {
        return json.readTree(response.body()).get("invoiceId").asString();
    }

    private static JsonNode withoutStatus(JsonNode invoice) {
        ObjectNode copy = (ObjectNode) invoice.deepCopy();
        copy.remove("status");
        return copy;
    }

    private void assertNotFound(String invoiceId) {
        HttpResponse<String> response = client.cancel(invoiceId);
        assertThat(response.statusCode()).as(invoiceId).isEqualTo(404);
        assertThat(response.body()).as(invoiceId).isEmpty();
    }
}
