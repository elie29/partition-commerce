package com.commerce.discount;

import static com.commerce.discount.DiscountClient.BASE_PATH;
import static com.commerce.discount.DiscountClient.discountRequest;
import static com.commerce.discount.DiscountClient.headers;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
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
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The use case "create a discount": BR-1 to BR-5, BR-7, BR-9 and BR-10 of the spec of {@code discount}, its Callers
 * row, and AR-1, AR-2 and AR-4 of {@code docs/specs/catalog/area.md} as they bind it.
 */
@DiscountApiTest
@ExtendWith(OutputCaptureExtension.class)
class CreateDiscountTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    @Autowired
    JdbcClient jdbc;

    DiscountClient client;

    @BeforeEach
    void client() {
        client = new DiscountClient(port);
    }

    @Test
    void creates_a_discount_answering_201_with_its_percent_and_its_minimum_order_as_sent() {
        HttpResponse<String> created = client.create(discountRequest(15, 5000, "EUR"));

        assertThat(created.statusCode()).isEqualTo(201);
        JsonNode discount = json.readTree(created.body());
        assertThat(discount.get("discountId").asString())
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(discount.get("percent").asInt()).isEqualTo(15);
        assertThat(discount.get("minimumOrder").get("amountMinor").asLong()).isEqualTo(5000);
        assertThat(discount.get("minimumOrder").get("currency").asString()).isEqualTo("EUR");
    }

    @Test
    void creates_a_discount_without_a_minimum_order_when_none_is_sent() {
        HttpResponse<String> created = client.create(discountRequest(10));

        assertThat(created.statusCode()).isEqualTo(201);
        JsonNode discount = json.readTree(created.body());
        assertThat(discount.has("minimumOrder") && !discount.get("minimumOrder").isNull()).isFalse();
    }

    @Test
    void accepts_1_and_90_percent_both_bounds_included() {
        for (long percent : new long[] {1, 90}) {
            HttpResponse<String> created = client.create(discountRequest(percent));

            assertThat(created.statusCode()).as(String.valueOf(percent)).isEqualTo(201);
            assertThat(json.readTree(created.body()).get("percent").asLong()).isEqualTo(percent);
        }
    }

    @Test
    void refuses_a_percent_outside_1_to_90_with_400_and_creates_nothing() {
        for (String percent : List.of("0", "91", "-5", "5000000000", "100000000000000000000000000000")) {
            long before = discounts();

            HttpResponse<String> refused = client.create("{\"percent\":" + percent + "}");

            assertThat(fields(refused)).as(percent).containsExactly("/percent");
            assertThat(discounts()).isEqualTo(before);
        }
    }

    @Test
    void refuses_a_percent_that_is_not_an_integer_or_is_missing() {
        for (String body : List.of("{\"percent\":12.5}", "{\"percent\":\"10\"}", "{\"percent\":null}", "{}")) {
            assertThat(fields(client.create(body))).as(body).containsExactly("/percent");
        }
    }

    @Test
    void refuses_a_negative_minimum_order_with_400_and_accepts_zero() {
        long before = discounts();

        assertThat(fields(client.create(discountRequest(10, -1, "EUR")))).containsExactly("/minimumOrder/amountMinor");
        assertThat(discounts()).isEqualTo(before);
        assertThat(client.create(discountRequest(10, 0, "EUR")).statusCode()).isEqualTo(201);
    }

    @Test
    void refuses_a_currency_that_is_not_a_known_code_with_400() {
        for (String currency : List.of("XYZ", "XAU", "xyz", "EURO")) {
            assertThat(fields(client.create(discountRequest(10, 100, currency)))).as(currency)
                    .containsExactly("/minimumOrder/currency");
        }
    }

    @Test
    void answers_every_field_at_fault_in_the_body_in_one_400() {
        HttpResponse<String> refused = client.create(discountRequest(0, -1, "XYZ"));

        assertThat(fields(refused)).containsExactlyInAnyOrder(
                "/percent", "/minimumOrder/amountMinor", "/minimumOrder/currency");
    }

    @Test
    void refuses_a_null_minimum_order_and_a_minimum_order_missing_its_fields() {
        assertThat(fields(client.create("{\"percent\":10,\"minimumOrder\":null}"))).containsExactly("/minimumOrder");
        assertThat(fields(client.create("{\"percent\":10,\"minimumOrder\":{}}")))
                .containsExactlyInAnyOrder("/minimumOrder/amountMinor", "/minimumOrder/currency");
    }

    @Test
    void refuses_a_currency_or_a_period_start_that_is_not_a_string_by_its_field() {
        assertThat(fields(client.create("{\"percent\":10,\"minimumOrder\":{\"amountMinor\":100,\"currency\":123}}")))
                .containsExactly("/minimumOrder/currency");
        assertThat(fields(client.create("{\"percent\":10,\"period\":{\"start\":5}}"))).containsExactly("/period/start");
    }

    @Test
    void accepts_a_period_and_keeps_none_of_it_an_end_before_its_start_included() {
        HttpResponse<String> created = client.create("{\"percent\":10,\"period\":"
                + "{\"start\":\"2026-12-31T00:00:00Z\",\"end\":\"2026-01-01T00:00:00Z\"}}");

        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(json.readTree(created.body()).has("period")).isFalse();
    }

    @Test
    void refuses_a_period_the_schema_refuses() {
        assertThat(fields(client.create("{\"percent\":10,\"period\":{\"end\":\"2026-01-01T00:00:00Z\"}}")))
                .containsExactly("/period/start");
        assertThat(fields(client.create("{\"percent\":10,\"period\":{\"start\":\"2026-01-01T00:00:00\"}}")))
                .containsExactly("/period/start");
        assertThat(fields(client.create("{\"percent\":10,\"period\":\"soon\"}"))).containsExactly("/period");
    }

    @Test
    void creates_two_identical_discounts_never_answering_409() {
        String body = discountRequest(20, 1000, "EUR");

        String first = json.readTree(client.create(body).body()).get("discountId").asString();
        HttpResponse<String> second = client.create(body);

        assertThat(second.statusCode()).isEqualTo(201);
        assertThat(json.readTree(second.body()).get("discountId").asString()).isNotEqualTo(first);
    }

    @Test
    void refuses_no_body_and_a_body_that_does_not_parse_with_400_one_item_and_no_field() {
        for (String body : new String[] {null, "{", "[]", "\"text\""}) {
            HttpResponse<String> refused = client.create(body);

            assertThat(refused.statusCode()).as(String.valueOf(body)).isEqualTo(400);
            JsonNode errors = json.readTree(refused.body()).get("errors");
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).get("code").asString()).isEqualTo("VALIDATION_FAILED");
            assertThat(errors.get(0).has("field") && !errors.get(0).get("field").isNull()).isFalse();
        }
    }

    @Test
    void refuses_a_missing_or_malformed_request_id_with_400_before_the_body() {
        for (String requestId : new String[] {null, "not-a-uuid", "1-1-1-1-1"}) {
            Map<String, String> headers = headers();
            if (requestId == null) {
                headers.remove("X-Request-Id");
            } else {
                headers.put("X-Request-Id", requestId);
            }

            HttpResponse<String> refused = client.create(discountRequest(0, -1, "XYZ"), headers);

            assertThat(fields(refused)).as(String.valueOf(requestId)).containsExactly("X-Request-Id");
        }
    }

    @Test
    void accept_language_an_authorization_header_and_an_unknown_property_change_nothing() {
        Map<String, String> headers = headers();
        headers.put("Accept-Language", "fr-FR");
        headers.put("Authorization", "Bearer not-a-token");

        HttpResponse<String> created = client.create("{\"percent\":30,\"foo\":{\"bar\":1}}", headers);

        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(json.readTree(created.body()).get("percent").asInt()).isEqualTo(30);
    }

    @Test
    void the_envelope_carries_its_path_an_error_id_a_detail_and_logs_no_amount(CapturedOutput output) {
        long amount = -987_654_321L;

        HttpResponse<String> refused = client.create(discountRequest(10, amount, "EUR"));

        JsonNode envelope = json.readTree(refused.body());
        assertThat(envelope.get("path").asString()).isEqualTo(BASE_PATH + "/discounts");
        String errorId = envelope.get("errorId").asString();
        assertThat(UUID.fromString(errorId)).isNotNull();
        assertThat(envelope.get("detail").asString()).isNotBlank();
        assertThat(refused.body()).doesNotContain("987654321");
        assertThat(output.getAll()).contains(errorId).contains("400").doesNotContain("987654321");
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

    private long discounts() {
        return jdbc.sql("SELECT COUNT(*) FROM discount").query(Long.class).single();
    }
}
