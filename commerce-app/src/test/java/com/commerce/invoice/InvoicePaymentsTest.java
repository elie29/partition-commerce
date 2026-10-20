package com.commerce.invoice;

import static com.commerce.invoice.InvoiceClient.line;
import static com.commerce.invoice.InvoiceClient.newOrderId;
import static com.commerce.invoice.InvoiceClient.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import com.commerce.invoice.application.InvoiceNotIssued;
import com.commerce.invoice.application.Invoices;
import com.commerce.invoice.application.InvoicePayments;
import com.commerce.invoice.application.InvoiceView;
import com.commerce.invoice.application.LineData;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.json.JsonMapper;

/**
 * The contract {@code InvoicePayments} through which {@code payment} reaches {@code invoice}, in process and over HTTP
 * (`docs/contracts/billing-internal.openapi.json`): read an invoice as {@code getInvoice} shows it, give its customer
 * (BR-32 of {@code payment}), record a payment on an invoice still issued (BR-3, BR-23), record a payment cancelled
 * (REQ-5, not built yet).
 */
@InvoiceApiTest
class InvoicePaymentsTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    @Autowired
    InvoicePayments invoicePayments;

    /** Spied on: a test makes the conditional update lose to a simultaneous change. */
    @MockitoSpyBean
    Invoices invoices;

    InvoiceClient client;

    @BeforeEach
    void client() {
        client = new InvoiceClient(port);
    }

    @Test
    void finds_an_invoice_as_getInvoice_shows_it() {
        UUID invoiceId = issue();

        InvoiceView found = invoicePayments.find(invoiceId).orElseThrow();

        assertThat(found.invoiceId()).isEqualTo(invoiceId);
        assertThat(found.status()).isEqualTo("ISSUED");
        assertThat(found.amountMinor()).isEqualTo(1449);
        assertThat(found.currency()).isEqualTo("EUR");
        assertThat(found.lines()).containsExactly(new LineData("Book", 1250, "EUR"), new LineData("Pen", 199, "EUR"));
    }

    @Test
    void finds_nothing_for_an_identifier_that_names_no_invoice() {
        assertThat(invoicePayments.find(UUID.randomUUID())).isEmpty();
        assertThat(invoicePayments.customerOf(UUID.randomUUID())).isEmpty();
    }

    @Test
    void gives_the_customer_of_an_invoice_for_the_fraud_review() {
        assertThat(invoicePayments.customerOf(issue())).contains("customer-1");
    }

    @Test
    void records_a_payment_changing_the_status_only() {
        UUID invoiceId = issue();
        InvoiceView issued = invoicePayments.find(invoiceId).orElseThrow();

        InvoiceView paid = invoicePayments.recordPayment(invoiceId);

        assertThat(paid.status()).isEqualTo("PAID");
        assertThat(paid).usingRecursiveComparison().ignoringFields("status").isEqualTo(issued);
        assertThatThrownBy(() -> invoicePayments.recordPayment(invoiceId)).isInstanceOf(InvoiceNotIssued.class);
        assertThat(invoicePayments.find(invoiceId).orElseThrow().status()).isEqualTo("PAID");
    }

    @Test
    void refuses_to_record_a_payment_on_a_cancelled_invoice() {
        UUID invoiceId = issue();
        assertThat(client.cancel(invoiceId.toString()).statusCode()).isEqualTo(200);

        assertThatThrownBy(() -> invoicePayments.recordPayment(invoiceId)).isInstanceOf(InvoiceNotIssued.class);
        assertThat(invoicePayments.find(invoiceId).orElseThrow().status()).isEqualTo("CANCELLED");
    }

    @Test
    void refuses_to_record_a_payment_on_a_paid_invoice() {
        UUID invoiceId = issue();
        assertThat(client.recordPayment(invoiceId.toString()).statusCode()).isEqualTo(200);

        assertThatThrownBy(() -> invoicePayments.recordPayment(invoiceId)).isInstanceOf(InvoiceNotIssued.class);
        assertThat(invoicePayments.find(invoiceId).orElseThrow().status()).isEqualTo("PAID");
    }

    @Test
    void refuses_to_record_a_payment_when_a_simultaneous_change_commits_first() {
        UUID invoiceId = issue();
        doReturn(false).when(invoices).changeStatus(eq(invoiceId), any(), any());

        assertThatThrownBy(() -> invoicePayments.recordPayment(invoiceId)).isInstanceOf(InvoiceNotIssued.class);
        assertThat(invoicePayments.find(invoiceId).orElseThrow().status()).isEqualTo("ISSUED");
    }

    @Test
    void does_not_record_a_payment_cancelled_until_req_5_is_built() {
        UUID invoiceId = issue();

        assertThatThrownBy(() -> invoicePayments.recordPaymentCancelled(invoiceId))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(invoicePayments.find(invoiceId).orElseThrow().status()).isEqualTo("ISSUED");
    }

    @Test
    void serves_an_invoice_over_http_as_getInvoice_shows_it() {
        UUID invoiceId = issue();

        HttpResponse<String> served = client.get("/internal/invoices/" + invoiceId);

        assertThat(served.statusCode()).isEqualTo(200);
        assertThat(json.readTree(served.body())).isEqualTo(json.readTree(client.read(invoiceId.toString()).body()));
        assertThat(json.readTree(served.body()).has("customerId")).isFalse();
    }

    @Test
    void serves_the_customer_of_an_invoice_over_http() {
        HttpResponse<String> served = client.get("/internal/invoices/" + issue() + "/customer");

        assertThat(served.statusCode()).isEqualTo(200);
        assertThat(json.readTree(served.body()).get("customerId").asString()).isEqualTo("customer-1");
    }

    @Test
    void answers_404_over_http_to_an_identifier_that_names_no_invoice_or_is_not_a_uuid() {
        for (String invoiceId : new String[] {UUID.randomUUID().toString(), "not-a-uuid"}) {
            for (HttpResponse<String> answer : java.util.List.of(
                    client.get("/internal/invoices/" + invoiceId),
                    client.get("/internal/invoices/" + invoiceId + "/customer"),
                    client.post("/internal/invoices/" + invoiceId + "/payment"))) {
                assertThat(answer.statusCode()).as(answer.uri().toString()).isEqualTo(404);
                assertThat(answer.body()).isEmpty();
            }
        }
    }

    @Test
    void records_a_payment_over_http_then_refuses_it_once_the_invoice_is_paid() {
        UUID invoiceId = issue();

        HttpResponse<String> recorded = client.post("/internal/invoices/" + invoiceId + "/payment");
        HttpResponse<String> again = client.post("/internal/invoices/" + invoiceId + "/payment");

        assertThat(recorded.statusCode()).isEqualTo(200);
        assertThat(json.readTree(recorded.body()).get("status").asString()).isEqualTo("PAID");
        assertThat(again.statusCode()).isEqualTo(409);
        assertThat(again.body()).isEmpty();
        assertThat(invoicePayments.find(invoiceId).orElseThrow().status()).isEqualTo("PAID");
    }

    @Test
    void answers_501_over_http_to_a_payment_cancelled_until_req_5_is_built() {
        UUID invoiceId = issue();

        assertThat(client.post("/internal/invoices/" + invoiceId + "/payment-cancellation").statusCode())
                .isEqualTo(501);
        assertThat(invoicePayments.find(invoiceId).orElseThrow().status()).isEqualTo("ISSUED");
    }

    private UUID issue() {
        HttpResponse<String> issued = client.issue(request(newOrderId(), "customer-1",
                line("Book", 1250, "EUR"), line("Pen", 199, "EUR")));
        assertThat(issued.statusCode()).isEqualTo(201);
        return UUID.fromString(json.readTree(issued.body()).get("invoiceId").asString());
    }
}
