package com.commerce.product;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Calls the HTTP API of {@code product} as an outside caller does, under the base path of the contract's
 * {@code servers}; builds the JSON of the requests and a fresh {@code X-Request-Id} per call unless a test sets one.
 */
final class ProductClient {

    static final String BASE_PATH = "/commerce/v1/catalog";

    private final HttpClient http = HttpClient.newHttpClient();
    private final String base;

    ProductClient(int port) {
        this.base = "http://localhost:" + port + BASE_PATH;
    }

    /** A reference no other test holds: three letters and six digits, as the contract's pattern says. */
    static String freshReference() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        char[] letters = {
            (char) ('A' + random.nextInt(26)), (char) ('A' + random.nextInt(26)), (char) ('A' + random.nextInt(26))
        };
        return new String(letters) + "-" + String.format("%06d", random.nextInt(1_000_000));
    }

    static String productRequest(String reference, String label) {
        return "{\"reference\":" + quoted(reference) + ",\"label\":" + quoted(label) + "}";
    }

    HttpResponse<String> create(String body) {
        return create(body, headers());
    }

    HttpResponse<String> create(String body, Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + "/products"))
                .header("Content-Type", "application/json")
                .POST(body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        headers.forEach(request::header);
        return send(request.build());
    }

    /** Creates a price list through the API of {@code price-list}, as a caller does; a test of the price needs one. */
    HttpResponse<String> createPriceList(String currency) {
        return write("POST", "/price-lists",
                "{\"name\":\"Shop\",\"currency\":" + quoted(currency) + ",\"channel\":\"SHOP\"}");
    }

    /** Sets a price through the API of {@code price-list}, as a caller does. */
    HttpResponse<String> setPrice(String priceListId, String productId, long amountMinor, String currency,
            String effectiveFrom) {
        return write("PUT", "/price-lists/" + priceListId + "/prices/" + productId,
                "{\"price\":{\"amountMinor\":" + amountMinor + ",\"currency\":" + quoted(currency) + "}"
                        + (effectiveFrom == null ? "" : ",\"effectiveFrom\":" + quoted(effectiveFrom)) + "}");
    }

    private HttpResponse<String> write(String method, String path, String body) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body));
        headers().forEach(request::header);
        return send(request.build());
    }

    HttpResponse<String> read(String productId) {
        return get("/products/" + productId, headers());
    }

    HttpResponse<String> get(String pathAndQuery) {
        return get(pathAndQuery, headers());
    }

    HttpResponse<String> get(String pathAndQuery, Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + pathAndQuery)).GET();
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
