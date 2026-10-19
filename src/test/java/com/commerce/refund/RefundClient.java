package com.commerce.refund;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * Calls the HTTP API as an outside caller does: the refund of {@code refund}, and the issue, read, cancel and pay that
 * a test of the refund needs; builds the JSON of the requests.
 */
final class RefundClient {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String base;

    RefundClient(int port) {
        this.base = "http://localhost:" + port;
    }

    HttpResponse<String> issue(String customerId, long amountMinor, String currency) {
        String json = "{\"orderId\":\"order-" + UUID.randomUUID() + "\",\"customerId\":" + quoted(customerId)
                + ",\"lines\":[{\"label\":\"Book\",\"price\":{\"amountMinor\":" + amountMinor
                + ",\"currency\":" + quoted(currency) + "}}]}";
        return post("/invoices", json);
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

    HttpResponse<String> pay(String invoiceId) {
        return post("/invoices/" + invoiceId + "/pay", "{\"cardToken\":\"tok_visa_4242\"}");
    }

    /** A refund with the body as given, so that a test can send what the contract refuses; {@code null}: no body. */
    HttpResponse<String> refund(String invoiceId, String body) {
        return post("/invoices/" + invoiceId + "/refunds", body);
    }

    static String refundRequest(long amountMinor, String currency) {
        return "{\"amount\":{\"amountMinor\":" + amountMinor + ",\"currency\":" + quoted(currency) + "}}";
    }

    private HttpResponse<String> post(String path, String body) {
        return send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
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

    private static String quoted(String value) {
        return value == null ? "null" : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
