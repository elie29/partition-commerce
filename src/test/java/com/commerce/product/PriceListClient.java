package com.commerce.product;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Calls the HTTP API of {@code price-list} as an outside caller does, under the base path of the contract's
 * {@code servers}; a fresh {@code X-Request-Id} per call unless a test sets the headers.
 */
final class PriceListClient {

    static final String BASE_PATH = "/commerce/v1/catalog";

    private final HttpClient http = HttpClient.newHttpClient();
    private final String base;

    PriceListClient(int port) {
        this.base = "http://localhost:" + port + BASE_PATH;
    }

    static String priceListRequest(String name, String currency, String channel) {
        return "{\"name\":" + quoted(name) + ",\"currency\":" + quoted(currency) + ",\"channel\":" + quoted(channel)
                + "}";
    }

    static String priceRequest(long amountMinor, String currency, String effectiveFrom) {
        return "{\"price\":{\"amountMinor\":" + amountMinor + ",\"currency\":" + quoted(currency) + "}"
                + (effectiveFrom == null ? "" : ",\"effectiveFrom\":" + quoted(effectiveFrom)) + "}";
    }

    HttpResponse<String> create(String body) {
        return create(body, headers());
    }

    HttpResponse<String> create(String body, Map<String, String> headers) {
        return send(write("/price-lists", body, headers).POST(publisher(body)).build());
    }

    HttpResponse<String> read(String priceListId) {
        return read(priceListId, headers());
    }

    HttpResponse<String> read(String priceListId, Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + "/price-lists/" + priceListId)).GET();
        headers.forEach(request::header);
        return send(request.build());
    }

    HttpResponse<String> setPrice(String priceListId, String productId, String body) {
        return setPrice(priceListId, productId, body, headers());
    }

    HttpResponse<String> setPrice(String priceListId, String productId, String body, Map<String, String> headers) {
        return send(write("/price-lists/" + priceListId + "/prices/" + productId, body, headers)
                .PUT(publisher(body))
                .build());
    }

    /** {@code getProductPrice}; {@code query} is appended as written, {@code ""} for none. */
    HttpResponse<String> productPrice(String priceListId, String productId, String query) {
        return productPrice(priceListId, productId, query, headers());
    }

    HttpResponse<String> productPrice(
            String priceListId, String productId, String query, Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(
                        URI.create(base + "/price-lists/" + priceListId + "/product-prices/" + productId + query))
                .GET();
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

    private HttpRequest.Builder write(String path, String body, Map<String, String> headers) {
        HttpRequest.Builder request =
                HttpRequest.newBuilder(URI.create(base + path)).header("Content-Type", "application/json");
        headers.forEach(request::header);
        return request;
    }

    private static HttpRequest.BodyPublisher publisher(String body) {
        return body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body);
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
