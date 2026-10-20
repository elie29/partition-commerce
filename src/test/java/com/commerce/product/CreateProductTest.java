package com.commerce.product;

import static com.commerce.product.ProductClient.BASE_PATH;
import static com.commerce.product.ProductClient.freshReference;
import static com.commerce.product.ProductClient.headers;
import static com.commerce.product.ProductClient.productRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
 * The use case "create a product": BR-1 to BR-5 of the spec of {@code product}, and BR-26 to BR-30 of
 * {@code docs/specs/catalog/product/spec.md} as they bind it.
 */
@ProductApiTest
@ExtendWith(OutputCaptureExtension.class)
class CreateProductTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    /** Counts the products recorded under a reference. */
    @Autowired
    JdbcClient jdbc;

    ProductClient client;

    @BeforeEach
    void client() {
        client = new ProductClient(port);
    }

    @Test
    void creates_a_product_answering_201_its_location_and_its_creation_time_to_the_millisecond() {
        String reference = freshReference();
        OffsetDateTime before = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        HttpResponse<String> created = client.create(productRequest(reference, "Le Petit Prince"));

        assertThat(created.statusCode()).isEqualTo(201);
        JsonNode product = json.readTree(created.body());
        String productId = product.get("productId").asString();
        assertThat(productId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(product.get("reference").asString()).isEqualTo(reference);
        assertThat(product.get("label").asString()).isEqualTo("Le Petit Prince");
        OffsetDateTime createdAt = OffsetDateTime.parse(product.get("createdAt").asString());
        assertThat(createdAt).isAfterOrEqualTo(before);
        assertThat(createdAt.getNano() % 1_000_000).isZero();
        assertThat(createdAt.getOffset().getTotalSeconds()).isZero();
        assertThat(created.headers().firstValue("Location")).hasValue(BASE_PATH + "/products/" + productId);
    }

    @Test
    void refuses_a_reference_another_product_holds_with_409_conflict_on_reference_and_creates_nothing() {
        String reference = freshReference();
        assertThat(client.create(productRequest(reference, "First")).statusCode()).isEqualTo(201);

        HttpResponse<String> again = client.create(productRequest(reference, "Second"));

        assertThat(again.statusCode()).isEqualTo(409);
        JsonNode envelope = json.readTree(again.body());
        assertThat(envelope.get("errors")).hasSize(1);
        assertThat(envelope.get("errors").get(0).get("code").asString()).isEqualTo("CONFLICT");
        assertThat(envelope.get("errors").get(0).get("field").asString()).isEqualTo("/reference");
        assertThat(productsWith(reference)).isEqualTo(1);
    }

    @Test
    void compares_references_exactly_as_written() {
        String reference = freshReference();
        assertThat(client.create(productRequest(reference, "First")).statusCode()).isEqualTo(201);

        String other = reference.substring(0, 4) + (reference.charAt(4) == '9' ? '0' : (char) (reference.charAt(4) + 1))
                + reference.substring(5);

        assertThat(client.create(productRequest(other, "Second")).statusCode()).isEqualTo(201);
    }

    @Test
    void of_two_creations_with_one_reference_at_the_same_moment_one_answers_201_and_the_other_409() throws Exception {
        String reference = freshReference();
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Integer>> calls = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String label = "Label " + i;
            calls.add(() -> {
                start.await();
                return client.create(productRequest(reference, label)).statusCode();
            });
        }
        List<Integer> statuses = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            List<Future<Integer>> futures = new ArrayList<>();
            for (Callable<Integer> call : calls) {
                futures.add(pool.submit(call));
            }
            start.countDown();
            for (Future<Integer> future : futures) {
                statuses.add(future.get());
            }
        }

        assertThat(statuses).containsExactlyInAnyOrder(201, 409);
        assertThat(productsWith(reference)).isEqualTo(1);
    }

    @Test
    void refuses_a_blank_label_with_400_non_breaking_spaces_included_and_creates_nothing() {
        for (String label : List.of("", "   ", " ", "    ")) {
            String reference = freshReference();

            HttpResponse<String> refused = client.create(productRequest(reference, label));

            assertThat(refused.statusCode()).as("[" + label + "]").isEqualTo(400);
            JsonNode errors = json.readTree(refused.body()).get("errors");
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).get("field").asString()).isEqualTo("/label");
            assertThat(errors.get(0).get("code").asString()).isEqualTo("VALIDATION_FAILED");
            assertThat(productsWith(reference)).isZero();
        }
    }

    @Test
    void answers_a_blank_label_and_a_reference_off_its_pattern_with_two_items() {
        HttpResponse<String> refused = client.create(productRequest("bok-1", " "));

        assertThat(refused.statusCode()).isEqualTo(400);
        assertThat(fields(refused)).containsExactlyInAnyOrder("/reference", "/label");
    }

    @Test
    void refuses_a_value_of_the_wrong_type_by_its_field_with_the_others_at_fault() {
        HttpResponse<String> refused = client.create("{\"reference\":123,\"label\":\" \","
                + "\"attributes\":{\"dimensions\":{\"widthMm\":1.5}}}");

        assertThat(fields(refused)).containsExactlyInAnyOrder("/reference", "/label", "/attributes/dimensions/widthMm");
    }

    @Test
    void counts_the_characters_of_a_label_as_code_points() {
        String emoji = "📚";
        assertThat(client.create(productRequest(freshReference(), emoji.repeat(120))).statusCode()).isEqualTo(201);

        HttpResponse<String> refused = client.create(productRequest(freshReference(), "a".repeat(121)));

        assertThat(refused.statusCode()).isEqualTo(400);
        assertThat(fields(refused)).containsExactly("/label");
    }

    @Test
    void refuses_what_the_contract_refuses_with_400_and_one_item_per_field_at_fault() {
        assertThat(fields(client.create("{\"label\":\"Book\"}"))).containsExactly("/reference");
        assertThat(fields(client.create("{\"reference\":" + ProductClient.quoted(freshReference()) + "}")))
                .containsExactly("/label");
        assertThat(fields(client.create("{\"reference\":\"BOK-000123\",\"label\":\"Book\","
                        + "\"attributes\":{\"gtin\":\"12\",\"translations\":[{\"language\":\"french\"}]}}")))
                .containsExactlyInAnyOrder("/attributes/gtin", "/attributes/translations/0/language",
                        "/attributes/translations/0/label");
    }

    @Test
    void refuses_no_body_and_a_body_that_does_not_parse_with_400_one_item_and_no_field() {
        for (String body : new String[] {null, "{", "[]"}) {
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
        for (String requestId : new String[] {null, "not-a-uuid", "1-1-1-1-1", ""}) {
            Map<String, String> headers = headers();
            if (requestId == null) {
                headers.remove("X-Request-Id");
            } else {
                headers.put("X-Request-Id", requestId);
            }

            HttpResponse<String> refused = client.create(productRequest(freshReference(), " "), headers);

            assertThat(refused.statusCode()).as(String.valueOf(requestId)).isEqualTo(400);
            assertThat(fields(refused)).containsExactly("X-Request-Id");
        }
    }

    @Test
    void accepts_and_keeps_none_of_description_ean_attributes_and_unknown_properties() {
        String reference = freshReference();
        String body = "{\"reference\":" + ProductClient.quoted(reference) + ",\"label\":\"Book\",\"description\":\"d\","
                + "\"ean\":\"123\",\"attributes\":{\"brand\":\"b\",\"gtin\":\"12345678\","
                + "\"dimensions\":{\"widthMm\":1},\"translations\":[{\"language\":\"fr-FR\",\"label\":\"Livre\"}]},"
                + "\"foo\":1}";

        HttpResponse<String> created = client.create(body);

        assertThat(created.statusCode()).isEqualTo(201);
        JsonNode product = json.readTree(created.body());
        assertThat(product.has("description")).isFalse();
        assertThat(product.has("ean")).isFalse();
        assertThat(product.has("attributes")).isFalse();
        JsonNode read = json.readTree(client.read(product.get("productId").asString()).body());
        assertThat(read.has("description") || read.has("ean") || read.has("attributes")).isFalse();
    }

    @Test
    void the_envelope_carries_its_path_an_error_id_a_detail_without_the_values_and_a_timestamp() {
        String reference = freshReference();

        HttpResponse<String> refused = client.create(productRequest(reference, " "));

        JsonNode envelope = json.readTree(refused.body());
        assertThat(envelope.get("path").asString()).isEqualTo(BASE_PATH + "/products");
        assertThat(UUID.fromString(envelope.get("errorId").asString())).isNotNull();
        assertThat(envelope.get("detail").asString()).isNotBlank().doesNotContain(reference);
        assertThat(OffsetDateTime.parse(envelope.get("timestamp").asString())).isNotNull();
        assertThat(envelope.get("errors").get(0).get("message").asString()).isNotBlank().doesNotContain(reference);
    }

    @Test
    void logs_the_error_id_the_request_id_the_path_and_the_status_never_a_label_a_reference_or_a_token(
            CapturedOutput output) {
        String reference = freshReference();
        String label = "Label-" + UUID.randomUUID();
        Map<String, String> headers = headers();
        headers.put("Authorization", "Bearer token-" + UUID.randomUUID());
        assertThat(client.create(productRequest(reference, label), headers).statusCode()).isEqualTo(201);

        HttpResponse<String> conflict = client.create(productRequest(reference, label), headers);

        String errorId = json.readTree(conflict.body()).get("errorId").asString();
        assertThat(output.getAll()).contains(errorId).contains(headers.get("X-Request-Id")).contains("409");
        assertThat(output.getAll()).doesNotContain(reference).doesNotContain(label)
                .doesNotContain(headers.get("Authorization").substring("Bearer ".length()));
    }

    @Test
    void never_logs_a_malformed_request_id(CapturedOutput output) {
        Map<String, String> headers = headers();
        String malformed = "malformed-" + UUID.randomUUID().toString().substring(0, 8);
        headers.put("X-Request-Id", malformed);

        HttpResponse<String> refused = client.create(productRequest(freshReference(), "Book"), headers);

        assertThat(refused.statusCode()).isEqualTo(400);
        assertThat(output.getAll()).contains(json.readTree(refused.body()).get("errorId").asString())
                .doesNotContain(malformed);
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

    private int productsWith(String reference) {
        return jdbc.sql("SELECT COUNT(*) FROM product WHERE reference = ?").param(reference).query(Integer.class)
                .single();
    }
}
