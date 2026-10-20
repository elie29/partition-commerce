package com.commerce.product;

import static com.commerce.product.PriceListClient.BASE_PATH;
import static com.commerce.product.PriceListClient.headers;
import static com.commerce.product.PriceListClient.priceListRequest;
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
 * The use case "create a price list": BR-13 to BR-16 and BR-22 of the spec of {@code product}, and AR-1 to AR-4 of
 * {@code docs/specs/catalog/area.md} as they bind it.
 */
@PriceListApiTest
@ExtendWith(OutputCaptureExtension.class)
class CreatePriceListTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    @Autowired
    JdbcClient jdbc;

    PriceListClient client;

    @BeforeEach
    void client() {
        client = new PriceListClient(port);
    }

    @Test
    void creates_a_price_list_answering_201_with_its_channel_and_currency() {
        String name = "Shop " + UUID.randomUUID();

        HttpResponse<String> created = client.create(priceListRequest(name, "EUR", "MARKETPLACE"));

        assertThat(created.statusCode()).isEqualTo(201);
        JsonNode list = json.readTree(created.body());
        assertThat(list.get("priceListId").asString())
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(list.get("name").asString()).isEqualTo(name);
        assertThat(list.get("currency").asString()).isEqualTo("EUR");
        assertThat(list.get("channel").asString()).isEqualTo("MARKETPLACE");
    }

    @Test
    void accepts_a_validity_and_keeps_none_of_it_a_validity_ending_before_it_starts_included() {
        String body = "{\"name\":\"Shop\",\"currency\":\"EUR\",\"channel\":\"SHOP\","
                + "\"validity\":{\"from\":\"2026-12-31\",\"to\":\"2026-01-01\"}}";

        HttpResponse<String> created = client.create(body);

        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(json.readTree(created.body()).has("validity")).isFalse();
    }

    @Test
    void creates_two_price_lists_with_one_name_never_answering_409() {
        String body = priceListRequest("Same name", "EUR", "SHOP");

        assertThat(client.create(body).statusCode()).isEqualTo(201);
        assertThat(client.create(body).statusCode()).isEqualTo(201);
    }

    @Test
    void refuses_a_blank_name_with_400_non_breaking_spaces_included_and_creates_nothing() {
        for (String name : List.of("", " ", "  ")) {
            long before = priceLists();

            HttpResponse<String> refused = client.create(priceListRequest(name, "EUR", "SHOP"));

            assertThat(fields(refused)).as("[" + name + "]").containsExactly("/name");
            assertThat(priceLists()).isEqualTo(before);
        }
    }

    @Test
    void refuses_a_currency_that_is_not_a_known_code_with_400() {
        for (String currency : List.of("XYZ", "XAU", "xyz", "EURO")) {
            assertThat(fields(client.create(priceListRequest("Shop", currency, "SHOP")))).as(currency)
                    .containsExactly("/currency");
        }
    }

    @Test
    void answers_every_field_at_fault_in_the_body_in_one_400() {
        HttpResponse<String> refused = client.create("{\"name\":\" \",\"currency\":\"XYZ\",\"channel\":\"TV\","
                + "\"validity\":{\"to\":\"not-a-date\"}}");

        assertThat(fields(refused))
                .containsExactlyInAnyOrder("/name", "/currency", "/channel", "/validity/from", "/validity/to");
    }

    @Test
    void refuses_a_value_of_the_wrong_type_by_its_field_with_the_others_at_fault() {
        HttpResponse<String> refused = client.create("{\"name\":42,\"currency\":\"EUR\"}");

        assertThat(fields(refused)).containsExactlyInAnyOrder("/name", "/channel");
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

            HttpResponse<String> refused = client.create(priceListRequest(" ", "XYZ", "TV"), headers);

            assertThat(fields(refused)).as(String.valueOf(requestId)).containsExactly("X-Request-Id");
        }
    }

    @Test
    void accept_language_and_an_unknown_property_change_nothing() {
        Map<String, String> headers = headers();
        headers.put("Accept-Language", "fr-FR");

        HttpResponse<String> created = client.create(
                "{\"name\":\"Shop\",\"currency\":\"EUR\",\"channel\":\"B2B\",\"foo\":{\"bar\":1}}", headers);

        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(json.readTree(created.body()).get("name").asString()).isEqualTo("Shop");
    }

    @Test
    void the_envelope_carries_its_path_an_error_id_a_detail_and_logs_no_value(CapturedOutput output) {
        String name = "secret-" + UUID.randomUUID();

        HttpResponse<String> refused = client.create(priceListRequest(name, "XYZ", "SHOP"));

        JsonNode envelope = json.readTree(refused.body());
        assertThat(envelope.get("path").asString()).isEqualTo(BASE_PATH + "/price-lists");
        String errorId = envelope.get("errorId").asString();
        assertThat(UUID.fromString(errorId)).isNotNull();
        assertThat(envelope.get("detail").asString()).isNotBlank().doesNotContain(name);
        assertThat(refused.body()).doesNotContain(name);
        assertThat(output.getAll()).contains(errorId).contains("400").doesNotContain(name);
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

    private long priceLists() {
        return jdbc.sql("SELECT COUNT(*) FROM price_list").query(Long.class).single();
    }
}
