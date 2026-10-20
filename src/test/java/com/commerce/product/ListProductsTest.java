package com.commerce.product;

import static com.commerce.product.ProductClient.freshReference;
import static com.commerce.product.ProductClient.headers;
import static com.commerce.product.ProductClient.productRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The use case "list the products": BR-6 and BR-7 of the spec of {@code product}, BR-27. */
@ProductApiTest
class ListProductsTest {

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
    void lists_the_products_by_reference_ascending_one_page_at_a_time_with_the_true_totals() {
        List<String> references = createdSorted(3);
        String filter = filterOf(references);

        JsonNode first = page(client.get("/products?" + filter + "&page=0&size=2"));
        JsonNode second = page(client.get("/products?" + filter + "&page=1&size=2"));

        assertThat(referencesOf(first)).containsExactly(references.get(0), references.get(1));
        assertThat(referencesOf(second)).containsExactly(references.get(2));
        assertThat(first.get("page").get("number").asInt()).isZero();
        assertThat(first.get("page").get("size").asInt()).isEqualTo(2);
        assertThat(first.get("page").get("totalElements").asLong()).isEqualTo(3);
        assertThat(first.get("page").get("totalPages").asInt()).isEqualTo(2);
        assertThat(first.get("items").get(0).has("createdAt")).isTrue();
    }

    @Test
    void a_page_past_the_last_answers_200_with_no_item_and_the_true_totals() {
        List<String> references = createdSorted(2);

        JsonNode past = page(client.get("/products?" + filterOf(references) + "&page=2147483647&size=100"));

        assertThat(past.get("items")).isEmpty();
        assertThat(past.get("page").get("totalElements").asLong()).isEqualTo(2);
        assertThat(past.get("page").get("totalPages").asInt()).isEqualTo(1);
    }

    @Test
    void compares_references_exactly_an_unknown_one_matches_nothing_and_one_given_twice_counts_once() {
        List<String> references = createdSorted(1);
        String known = references.getFirst();

        JsonNode twice = page(client.get("/products?references=" + known + "&references=" + known));
        JsonNode unknown = page(client.get("/products?references=" + freshReference()));

        assertThat(referencesOf(twice)).containsExactly(known);
        assertThat(twice.get("page").get("totalElements").asLong()).isEqualTo(1);
        assertThat(unknown.get("items")).isEmpty();
        assertThat(unknown.get("page").get("totalElements").asLong()).isZero();
    }

    @Test
    void an_empty_references_is_no_filter() {
        createdSorted(2);

        JsonNode all = page(client.get("/products?references=&size=100"));

        assertThat(all.get("page").get("totalElements").asLong()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void localized_and_accept_language_change_nothing() {
        List<String> references = createdSorted(1);
        Map<String, String> headers = headers();
        headers.put("Accept-Language", "fr-FR");

        JsonNode plain = page(client.get("/products?" + filterOf(references)));
        JsonNode localized = page(client.get("/products?" + filterOf(references) + "&localized=true", headers));

        assertThat(localized.get("items")).isEqualTo(plain.get("items"));
    }

    @Test
    void refuses_what_the_schema_refuses_with_400_and_one_item_per_query_parameter_at_fault() {
        assertThat(fields(client.get("/products?page=-1"))).containsExactly("page");
        assertThat(fields(client.get("/products?page=2147483648"))).containsExactly("page");
        assertThat(fields(client.get("/products?page=abc"))).containsExactly("page");
        assertThat(fields(client.get("/products?size=0&page=-1"))).containsExactlyInAnyOrder("size", "page");
        assertThat(fields(client.get("/products?size=101"))).containsExactly("size");
        assertThat(fields(client.get("/products?localized=maybe"))).containsExactly("localized");
    }

    @Test
    void refuses_a_missing_request_id_with_400_before_the_query() {
        Map<String, String> headers = headers();
        headers.remove("X-Request-Id");

        assertThat(fields(client.get("/products?page=-1", headers))).containsExactly("X-Request-Id");
    }

    private List<String> createdSorted(int count) {
        List<String> references = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String reference = freshReference();
            assertThat(client.create(productRequest(reference, "Book " + i)).statusCode()).isEqualTo(201);
            references.add(reference);
        }
        references.sort(String::compareTo);
        return references;
    }

    private static String filterOf(List<String> references) {
        return String.join("&", references.stream().map(r -> "references=" + r).toList());
    }

    private JsonNode page(HttpResponse<String> response) {
        assertThat(response.statusCode()).isEqualTo(200);
        return json.readTree(response.body());
    }

    private static List<String> referencesOf(JsonNode page) {
        List<String> references = new ArrayList<>();
        page.get("items").forEach(item -> references.add(item.get("reference").asString()));
        return references;
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
