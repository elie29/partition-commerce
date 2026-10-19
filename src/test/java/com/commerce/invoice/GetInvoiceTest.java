package com.commerce.invoice;

import static com.commerce.invoice.InvoiceClient.line;
import static com.commerce.invoice.InvoiceClient.newOrderId;
import static com.commerce.invoice.InvoiceClient.request;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The use case "read an invoice back": BR-15, BR-17 of the spec of {@code invoice}. */
@InvoiceApiTest
class GetInvoiceTest {

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
    void reads_back_the_invoice_as_it_was_issued_with_its_lines_in_order_and_without_its_customer() {
        String orderId = newOrderId();
        HttpResponse<String> issued = client.issue(request(orderId, "customer-1",
                line("Pen", 199, "EUR"), line("Book", 1250, "EUR"), line("Apple", 0, "EUR")));
        String invoiceId = json.readTree(issued.body()).get("invoiceId").asString();

        HttpResponse<String> read = client.read(invoiceId);

        assertThat(read.statusCode()).isEqualTo(200);
        JsonNode invoice = json.readTree(read.body());
        assertThat(invoice).isEqualTo(json.readTree(issued.body()));
        assertThat(invoice.get("lines").valueStream().map(l -> l.get("label").asString()).toList())
                .containsExactly("Pen", "Book", "Apple");
        assertThat(invoice.get("amount").get("amountMinor").asLong()).isEqualTo(1449);
        assertThat(invoice.has("customerId")).isFalse();
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

    private void assertNotFound(String invoiceId) {
        HttpResponse<String> response = client.read(invoiceId);
        assertThat(response.statusCode()).as(invoiceId).isEqualTo(404);
        assertThat(response.body()).as(invoiceId).isEmpty();
    }
}
