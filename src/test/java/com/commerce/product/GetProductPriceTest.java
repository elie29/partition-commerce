package com.commerce.product;

import static com.commerce.product.PriceListClient.headers;
import static com.commerce.product.PriceListClient.priceListRequest;
import static com.commerce.product.PriceListClient.priceRequest;
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
 * The use case "read the price of a product": BR-21, BR-23 and BR-24 of the spec of {@code product}, its Applies
 * and Day; AR-1 to AR-3 of the area. A product is an identifier only: {@code price-list} does not read {@code product}.
 */
@PriceListApiTest
@ExtendWith(OutputCaptureExtension.class)
class GetProductPriceTest {

    private static final String NOT_FOUND_DETAIL = "No such product, or no price for it in that price list.";

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    PriceListClient client;

    @BeforeEach
    void client() {
        client = new PriceListClient(port);
    }

    @Test
    void answers_the_price_that_applies_today_when_at_is_absent() {
        String productId = UUID.randomUUID().toString();
        String listId = list("EUR");
        assertThat(setPrice(listId, productId, 1999, null)).isEqualTo(200);

        HttpResponse<String> read = client.productPrice(listId, productId, "");

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
        String productId = UUID.randomUUID().toString();
        String listId = list("EUR");
        setPrice(listId, productId, 100, "2030-06-15T23:59:59.999Z");

        assertThat(status(listId, productId, "2030-06-15")).isEqualTo(200);
        assertThat(status(listId, productId, "2030-06-14")).isEqualTo(404);

        setPrice(listId, productId, 100, "2030-06-16T00:00:00Z");

        assertThat(status(listId, productId, "2030-06-15")).isEqualTo(404);
        assertThat(status(listId, productId, "2030-06-16")).isEqualTo(200);
    }

    @Test
    void a_price_effective_later_today_applies_today_by_the_day() {
        String productId = UUID.randomUUID().toString();
        String listId = list("EUR");
        String laterToday = Instant.now().truncatedTo(ChronoUnit.DAYS).plusSeconds(86_399).toString();
        setPrice(listId, productId, 100, laterToday);

        assertThat(client.productPrice(listId, productId, "").statusCode()).isEqualTo(200);
    }

    @Test
    void a_future_price_hides_the_price_until_its_day_and_a_past_day_is_answered_against_the_price_held_now() {
        String productId = UUID.randomUUID().toString();
        String listId = list("EUR");
        setPrice(listId, productId, 100, "2020-01-01T00:00:00Z");
        assertThat(status(listId, productId, "2025-01-01")).isEqualTo(200);

        String tomorrow = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS).toString();
        setPrice(listId, productId, 200, tomorrow);

        assertNotFound(client.productPrice(listId, productId, ""));
        assertThat(status(listId, productId, "2025-01-01")).isEqualTo(404);
        assertThat(status(listId, productId, tomorrow.substring(0, 10))).isEqualTo(200);
    }

    @Test
    void answers_one_404_price_not_found_for_no_price_list_no_price_or_an_identifier_not_a_uuid() {
        String productId = UUID.randomUUID().toString();
        String listId = list("EUR");
        setPrice(listId, productId, 100, null);

        assertNotFound(client.productPrice(listId, UUID.randomUUID().toString(), ""));
        assertNotFound(client.productPrice(UUID.randomUUID().toString(), productId, ""));
        assertNotFound(client.productPrice(listId, "1-1-1-1-1", ""));
        assertNotFound(client.productPrice("1-1-1-1-1", productId, ""));
    }

    @Test
    void checks_at_before_the_identifiers() {
        assertThat(fields(client.productPrice("abc", "1-1-1-1-1", "?at=2026-13-01"))).containsExactly("at");
        assertThat(fields(client.productPrice("abc", "abc", "?at=2026-10-19+01:00"))).containsExactly("at");
    }

    @Test
    void refuses_a_missing_request_id_with_400_before_the_query() {
        Map<String, String> headers = headers();
        headers.remove("X-Request-Id");

        assertThat(fields(client.productPrice("abc", "abc", "?at=x", headers))).containsExactly("X-Request-Id");
    }

    @Test
    void no_output_names_the_amount(CapturedOutput output) {
        String productId = UUID.randomUUID().toString();
        String listId = list("EUR");
        long amount = 8_765_432_109L;
        setPrice(listId, productId, amount, null);

        HttpResponse<String> read = client.productPrice(listId, productId, "");

        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(output.getAll()).doesNotContain(String.valueOf(amount));
    }

    private String list(String currency) {
        HttpResponse<String> created = client.create(priceListRequest("Shop", currency, "SHOP"));
        assertThat(created.statusCode()).isEqualTo(201);
        return json.readTree(created.body()).get("priceListId").asString();
    }

    private int setPrice(String listId, String productId, long amountMinor, String effectiveFrom) {
        return client.setPrice(listId, productId, priceRequest(amountMinor, "EUR", effectiveFrom)).statusCode();
    }

    private int status(String listId, String productId, String at) {
        return client.productPrice(listId, productId, "?at=" + at).statusCode();
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
