package com.commerce.product;

import static com.commerce.product.ProductClient.freshReference;
import static com.commerce.product.ProductClient.headers;
import static com.commerce.product.ProductClient.productRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The use case "read the price of a product": BR-9 and BR-10 of the spec of {@code product}, through
 * {@code PriceListPrices} of {@code price-list}; AR-1 to AR-3, Applies and Day of the area.
 */
@ProductApiTest
@ExtendWith(OutputCaptureExtension.class)
class GetProductPriceTest {

    private static final String NOT_FOUND_DETAIL = "No such product, or no price for it in that price list.";

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    ProductClient client;

    @BeforeEach
    void client() {
        client = new ProductClient(port);
    }

    @Test
    void answers_the_price_that_applies_today_when_at_is_absent() {
        String productId = product();
        String listId = list("EUR");
        assertThat(client.setPrice(listId, productId, 1999, "EUR", null).statusCode()).isEqualTo(200);

        HttpResponse<String> read = client.get("/products/" + productId + "/price?priceListId=" + listId);

        assertThat(read.statusCode()).isEqualTo(200);
        JsonNode price = json.readTree(read.body());
        assertThat(price.get("productId").asString()).isEqualTo(productId);
        assertThat(price.get("price").get("amountMinor").asLong()).isEqualTo(1999);
        assertThat(price.get("price").get("currency").asString()).isEqualTo("EUR");
        assertThat(OffsetDateTime.parse(price.get("effectiveFrom").asString()).toInstant())
                .isBefore(Instant.now().plusSeconds(1));
    }

    @Test
    void a_price_applies_on_day_at_up_to_the_last_millisecond_of_that_day_in_utc() {
        String productId = product();
        String listId = list("EUR");
        client.setPrice(listId, productId, 100, "EUR", "2030-06-15T23:59:59.999Z");

        assertThat(status(productId, listId, "2030-06-15")).isEqualTo(200);
        assertThat(status(productId, listId, "2030-06-14")).isEqualTo(404);

        client.setPrice(listId, productId, 100, "EUR", "2030-06-16T00:00:00Z");

        assertThat(status(productId, listId, "2030-06-15")).isEqualTo(404);
        assertThat(status(productId, listId, "2030-06-16")).isEqualTo(200);
    }

    @Test
    void a_price_effective_later_today_applies_today_by_the_day() {
        String productId = product();
        String listId = list("EUR");
        String laterToday = Instant.now().truncatedTo(ChronoUnit.DAYS).plusSeconds(86_399).toString();
        client.setPrice(listId, productId, 100, "EUR", laterToday);

        assertThat(client.get("/products/" + productId + "/price?priceListId=" + listId).statusCode()).isEqualTo(200);
    }

    @Test
    void a_future_price_hides_the_price_until_its_day_and_a_past_day_is_answered_against_the_price_held_now() {
        String productId = product();
        String listId = list("EUR");
        client.setPrice(listId, productId, 100, "EUR", "2020-01-01T00:00:00Z");
        assertThat(status(productId, listId, "2025-01-01")).isEqualTo(200);

        String tomorrow = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS).toString();
        client.setPrice(listId, productId, 200, "EUR", tomorrow);

        HttpResponse<String> today = client.get("/products/" + productId + "/price?priceListId=" + listId);
        assertNotFound(today);
        assertThat(status(productId, listId, "2025-01-01")).isEqualTo(404);
        assertThat(status(productId, listId, tomorrow.substring(0, 10))).isEqualTo(200);
    }

    @Test
    void answers_one_404_price_not_found_for_no_product_no_price_list_or_no_price() {
        String productId = product();
        String listId = list("EUR");
        String unpriced = product();
        client.setPrice(listId, productId, 100, "EUR", null);

        assertNotFound(client.get("/products/" + UUID.randomUUID() + "/price?priceListId=" + listId));
        assertNotFound(client.get("/products/1-1-1-1-1/price?priceListId=" + listId));
        assertNotFound(client.get("/products/" + productId + "/price?priceListId=" + UUID.randomUUID()));
        assertNotFound(client.get("/products/" + unpriced + "/price?priceListId=" + listId));
    }

    @Test
    void checks_its_query_before_the_product_one_item_per_parameter_at_fault() {
        String unknown = UUID.randomUUID().toString();

        assertThat(fields(client.get("/products/" + unknown + "/price?priceListId=1-1-1-1-1")))
                .containsExactly("priceListId");
        assertThat(fields(client.get("/products/" + unknown + "/price"))).containsExactly("priceListId");
        assertThat(fields(client.get("/products/" + unknown + "/price?priceListId=" + unknown + "&at=2026-13-01")))
                .containsExactly("at");
        assertThat(fields(client.get("/products/" + unknown + "/price?priceListId=abc&at=2026-10-19+01:00")))
                .containsExactlyInAnyOrder("priceListId", "at");
    }

    @Test
    void refuses_a_missing_request_id_with_400_before_the_query() {
        Map<String, String> headers = headers();
        headers.remove("X-Request-Id");

        assertThat(fields(client.get("/products/1-1-1-1-1/price?priceListId=abc", headers)))
                .containsExactly("X-Request-Id");
    }

    @Test
    void no_output_names_the_amount_read_from_price_list(CapturedOutput output) {
        String productId = product();
        String listId = list("EUR");
        long amount = 8_765_432_109L;
        client.setPrice(listId, productId, amount, "EUR", null);

        HttpResponse<String> read = client.get("/products/" + productId + "/price?priceListId=" + listId);

        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(output.getAll()).doesNotContain(String.valueOf(amount));
    }

    private String product() {
        HttpResponse<String> created = client.create(productRequest(freshReference(), "Book"));
        assertThat(created.statusCode()).isEqualTo(201);
        return json.readTree(created.body()).get("productId").asString();
    }

    private String list(String currency) {
        HttpResponse<String> created = client.createPriceList(currency);
        assertThat(created.statusCode()).isEqualTo(201);
        return json.readTree(created.body()).get("priceListId").asString();
    }

    private int status(String productId, String listId, String at) {
        return client.get("/products/" + productId + "/price?priceListId=" + listId + "&at=" + at).statusCode();
    }

    private void assertNotFound(HttpResponse<String> response) {
        assertThat(response.statusCode()).isEqualTo(404);
        JsonNode envelope = json.readTree(response.body());
        assertThat(envelope.get("detail").asString()).isEqualTo(NOT_FOUND_DETAIL);
        assertThat(envelope.get("errors")).hasSize(1);
        assertThat(envelope.get("errors").get(0).get("code").asString()).isEqualTo("PRICE_NOT_FOUND");
    }

    private List<String> fields(HttpResponse<String> response) {
        assertThat(response.statusCode()).isEqualTo(400);
        List<String> fields = new ArrayList<>();
        for (JsonNode item : json.readTree(response.body()).get("errors")) {
            assertThat(item.get("code").asString()).isEqualTo("VALIDATION_FAILED");
            fields.add(item.get("field").asString());
        }
        return fields;
    }
}
