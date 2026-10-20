package com.commerce.invoice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/** Calls the HTTP API of {@code invoice} as an outside caller does, and builds the JSON of an issue request. */
final class InvoiceClient {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String base;

    InvoiceClient(int port) {
        this.base = "http://localhost:" + port;
    }

    HttpResponse<String> issue(String json) {
        return send(HttpRequest.newBuilder(URI.create(base + "/invoices"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build());
    }

    HttpResponse<String> read(String invoiceId) {
        return send(HttpRequest.newBuilder(URI.create(base + "/invoices/" + invoiceId))
                .header("Accept", "application/json")
                .GET()
                .build());
    }

    HttpResponse<String> cancel(String invoiceId) {
        return send(HttpRequest.newBuilder(URI.create(base + "/invoices/" + invoiceId + "/cancel"))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build());
    }

    /** The invoice recorded paid, as {@code payment-app} records it (`docs/contracts/billing-internal.openapi.json`). */
    HttpResponse<String> recordPayment(String invoiceId) {
        return post("/internal/invoices/" + invoiceId + "/payment");
    }

    /** A GET of JSON, to a path under the base, such as {@code /internal/invoices/{invoiceId}}. */
    HttpResponse<String> get(String path) {
        return send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Accept", "application/json")
                .GET()
                .build());
    }

    /** A POST with no body, to a path under the base, such as {@code /internal/invoices/{invoiceId}/payment}. */
    HttpResponse<String> post(String path) {
        return send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build());
    }

    /** A POST with no content type and no body, to a path under the base, such as {@code /invoices}. */
    HttpResponse<String> postWithoutContentType(String path) {
        return send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    static String newOrderId() {
        return "order-" + UUID.randomUUID();
    }

    static String request(String orderId, String customerId, String... lines) {
        return "{\"orderId\":" + quoted(orderId) + ",\"customerId\":" + quoted(customerId)
                + ",\"lines\":[" + String.join(",", lines) + "]}";
    }

    static String line(String label, long amountMinor, String currency) {
        return "{\"label\":" + quoted(label) + ",\"price\":{\"amountMinor\":" + amountMinor
                + ",\"currency\":" + quoted(currency) + "}}";
    }

    private static String quoted(String value) {
        return value == null ? "null" : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
