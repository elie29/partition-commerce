package com.commerce.discount;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Calls the HTTP API of {@code discount} as an outside caller does, under the base path of the contract's
 * {@code servers}; a fresh {@code X-Request-Id} per call unless a test sets the headers.
 */
final class DiscountClient {

    static final String BASE_PATH = "/commerce/v1/catalog";

    private final HttpClient http = HttpClient.newHttpClient();
    private final String base;

    DiscountClient(int port) {
        this.base = "http://localhost:" + port + BASE_PATH;
    }

    static String discountRequest(long percent) {
        return "{\"percent\":" + percent + "}";
    }

    static String discountRequest(long percent, long amountMinor, String currency) {
        return "{\"percent\":" + percent + ",\"minimumOrder\":{\"amountMinor\":" + amountMinor + ",\"currency\":"
                + quoted(currency) + "}}";
    }

    HttpResponse<String> create(String body) {
        return create(body, headers());
    }

    HttpResponse<String> create(String body, Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + "/discounts"))
                .header("Content-Type", "application/json")
                .POST(body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        headers.forEach(request::header);
        return send(request.build());
    }

    HttpResponse<String> read(String discountId) {
        return read(discountId, headers());
    }

    HttpResponse<String> read(String discountId, Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + "/discounts/" + discountId)).GET();
        headers.forEach(request::header);
        return send(request.build());
    }

    /** {@code Accept} and a fresh, well-formed {@code X-Request-Id}; a test removes or replaces either. */
    static Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        headers.put("X-Request-Id", UUID.randomUUID().toString());
        return headers;
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

    static String quoted(String value) {
        return value == null ? "null" : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
