package com.commerce.product;

import static com.commerce.product.PriceListClient.headers;
import static com.commerce.product.PriceListClient.priceListRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.commerce.product.application.PriceLists;
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
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The use case "read a price list": BR-13, BR-15, BR-18 and BR-22 of the spec of {@code product}, AR-1 to AR-3. */
@PriceListApiTest
@ExtendWith(OutputCaptureExtension.class)
class GetPriceListTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    /** Writes a price list as an earlier JDK would have let it be kept. */
    @Autowired
    JdbcClient jdbc;

    /** Spied on: a test makes the store fail. */
    @MockitoSpyBean
    PriceLists priceLists;

    PriceListClient client;

    @BeforeEach
    void client() {
        client = new PriceListClient(port);
    }

    @Test
    void reads_a_price_list_as_it_was_created() {
        JsonNode created = json.readTree(client.create(priceListRequest("Shop", "GBP", "B2B")).body());

        HttpResponse<String> read = client.read(created.get("priceListId").asString());

        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(json.readTree(read.body())).isEqualTo(created);
    }

    /** BR-13 is checked on incoming requests only: a code a later JDK drops is read as it was kept. */
    @Test
    void reads_a_price_list_stored_in_a_code_the_jdk_does_not_list() {
        UUID priceListId = UUID.randomUUID();
        jdbc.sql("INSERT INTO price_list (price_list_id, name, currency, channel) VALUES (?, 'Old', 'ZZZ', 'SHOP')")
                .param(priceListId)
                .update();

        HttpResponse<String> read = client.read(priceListId.toString());

        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(json.readTree(read.body()).get("currency").asString()).isEqualTo("ZZZ");
    }

    @Test
    void answers_404_price_list_not_found_for_an_identifier_that_names_no_list_or_is_not_a_uuid() {
        for (String priceListId : List.of(UUID.randomUUID().toString(), "42", "1-1-1-1-1",
                UUID.randomUUID().toString().replace("-", ""))) {
            HttpResponse<String> response = client.read(priceListId);

            assertThat(response.statusCode()).as(priceListId).isEqualTo(404);
            JsonNode errors = json.readTree(response.body()).get("errors");
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).get("code").asString()).isEqualTo("PRICE_LIST_NOT_FOUND");
            assertThat(errors.get(0).has("field") && !errors.get(0).get("field").isNull()).isFalse();
        }
    }

    @Test
    void a_failure_of_the_store_answers_500_with_the_envelope_and_logs_the_exception_s_class_and_frames_not_its_message(
            CapturedOutput output) {
        String secret = "secret-" + UUID.randomUUID();
        doThrow(new DataAccessResourceFailureException("the database is down: " + secret))
                .when(priceLists).find(any());

        HttpResponse<String> response = client.read(UUID.randomUUID().toString());

        assertThat(response.statusCode()).isEqualTo(500);
        JsonNode envelope = json.readTree(response.body());
        assertThat(envelope.get("errors")).isEmpty();
        assertThat(envelope.get("detail").asString()).isNotBlank();
        String errorId = envelope.get("errorId").asString();
        assertThat(output.getAll()).contains(errorId).contains("status=500")
                .contains(DataAccessResourceFailureException.class.getName())
                .contains("at com.commerce.product.application.GetPriceListUseCase")
                .doesNotContain(secret);
        assertThat(response.body()).doesNotContain(secret);
    }

    @Test
    void refuses_a_missing_request_id_with_400_before_the_identifier() {
        Map<String, String> headers = headers();
        headers.remove("X-Request-Id");

        HttpResponse<String> response = client.read("1-1-1-1-1", headers);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(json.readTree(response.body()).get("errors").get(0).get("field").asString())
                .isEqualTo("X-Request-Id");
    }
}
