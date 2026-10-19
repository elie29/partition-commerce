package com.commerce.product;

import static com.commerce.product.ProductClient.freshReference;
import static com.commerce.product.ProductClient.headers;
import static com.commerce.product.ProductClient.productRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.commerce.product.application.Products;
import java.net.http.HttpResponse;
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
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The use case "read a product": BR-5, BR-6 and BR-8 of the spec of {@code product}, AR-1 to AR-3. */
@ProductApiTest
@ExtendWith(OutputCaptureExtension.class)
class GetProductTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    /** Spied on: a test makes the store fail. */
    @MockitoSpyBean
    Products products;

    ProductClient client;

    @BeforeEach
    void client() {
        client = new ProductClient(port);
    }

    @Test
    void reads_a_product_as_it_was_created_its_creation_time_included() {
        JsonNode created = json.readTree(client.create(productRequest(freshReference(), "Book")).body());

        HttpResponse<String> read = client.read(created.get("productId").asString());

        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(json.readTree(read.body())).isEqualTo(created);
    }

    @Test
    void answers_404_product_not_found_for_an_identifier_that_names_no_product_or_is_not_a_uuid() {
        for (String productId : List.of(UUID.randomUUID().toString(), "42", "not-a-uuid", "1-1-1-1-1",
                UUID.randomUUID().toString().replace("-", ""))) {
            HttpResponse<String> response = client.read(productId);

            assertThat(response.statusCode()).as(productId).isEqualTo(404);
            JsonNode errors = json.readTree(response.body()).get("errors");
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).get("code").asString()).isEqualTo("PRODUCT_NOT_FOUND");
            assertThat(errors.get(0).has("field") && !errors.get(0).get("field").isNull()).isFalse();
        }
    }

    @Test
    void answers_the_label_given_at_creation_whatever_accept_language_says() {
        String productId = json.readTree(client.create(productRequest(freshReference(), "Book")).body())
                .get("productId").asString();
        Map<String, String> headers = headers();
        headers.put("Accept-Language", "fr-FR");

        HttpResponse<String> read = client.get("/products/" + productId, headers);

        assertThat(json.readTree(read.body()).get("label").asString()).isEqualTo("Book");
    }

    @Test
    void a_failure_of_the_store_answers_500_with_the_envelope_and_logs_the_exception_s_class_and_frames_not_its_message(
            CapturedOutput output) {
        String secret = "secret-" + UUID.randomUUID();
        doThrow(new DataAccessResourceFailureException("the database is down: " + secret)).when(products).find(any());

        HttpResponse<String> response = client.read(UUID.randomUUID().toString());

        assertThat(response.statusCode()).isEqualTo(500);
        JsonNode envelope = json.readTree(response.body());
        assertThat(envelope.get("errors")).isEmpty();
        assertThat(envelope.get("detail").asString()).isNotBlank();
        String errorId = envelope.get("errorId").asString();
        assertThat(output.getAll()).contains(errorId).contains("status=500")
                .contains(DataAccessResourceFailureException.class.getName())
                .contains("at com.commerce.product.application.GetProductUseCase")
                .doesNotContain(secret);
        assertThat(response.body()).doesNotContain(secret);
    }

    @Test
    void refuses_a_missing_request_id_with_400_before_the_identifier() {
        Map<String, String> headers = headers();
        headers.remove("X-Request-Id");

        HttpResponse<String> response = client.get("/products/not-a-uuid", headers);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(json.readTree(response.body()).get("errors").get(0).get("field").asString())
                .isEqualTo("X-Request-Id");
    }
}
