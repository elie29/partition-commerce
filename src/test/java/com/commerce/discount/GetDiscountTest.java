package com.commerce.discount;

import static com.commerce.discount.DiscountClient.discountRequest;
import static com.commerce.discount.DiscountClient.headers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.commerce.discount.application.Discounts;
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

/** The use case "read a discount": BR-2, BR-4, BR-5, BR-8 and BR-10 of the spec of {@code discount}, AR-1 to AR-3. */
@DiscountApiTest
@ExtendWith(OutputCaptureExtension.class)
class GetDiscountTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    /** Writes a discount as an earlier JDK would have let it be kept. */
    @Autowired
    JdbcClient jdbc;

    /** Spied on: a test makes the store fail. */
    @MockitoSpyBean
    Discounts discounts;

    DiscountClient client;

    @BeforeEach
    void client() {
        client = new DiscountClient(port);
    }

    @Test
    void reads_a_discount_as_it_was_created_with_and_without_a_minimum_order() {
        for (String body : List.of(discountRequest(25, 7500, "GBP"), discountRequest(5))) {
            JsonNode created = json.readTree(client.create(body).body());

            HttpResponse<String> read = client.read(created.get("discountId").asString());

            assertThat(read.statusCode()).as(body).isEqualTo(200);
            assertThat(json.readTree(read.body())).as(body).isEqualTo(created);
        }
    }

    @Test
    void reads_no_period_the_period_is_not_kept() {
        JsonNode created = json.readTree(client.create("{\"percent\":10,\"period\":"
                + "{\"start\":\"2026-12-31T00:00:00Z\",\"end\":\"2026-01-01T00:00:00Z\"}}").body());

        HttpResponse<String> read = client.read(created.get("discountId").asString());

        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(json.readTree(read.body()).has("period")).isFalse();
    }

    /** BR-4 is checked on incoming requests only: a code a later JDK drops is read as it was kept. */
    @Test
    void reads_a_discount_stored_in_a_code_the_jdk_does_not_list() {
        UUID discountId = UUID.randomUUID();
        jdbc.sql("INSERT INTO discount (discount_id, percent, minimum_order_minor, minimum_order_currency) "
                        + "VALUES (?, 10, 100, 'ZZZ')")
                .param(discountId)
                .update();

        HttpResponse<String> read = client.read(discountId.toString());

        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(json.readTree(read.body()).get("minimumOrder").get("currency").asString()).isEqualTo("ZZZ");
    }

    @Test
    void answers_404_discount_not_found_for_an_identifier_that_names_no_discount_or_is_not_a_uuid() {
        for (String discountId : List.of(UUID.randomUUID().toString(), "42", "1-1-1-1-1",
                UUID.randomUUID().toString().replace("-", ""))) {
            HttpResponse<String> response = client.read(discountId);

            assertThat(response.statusCode()).as(discountId).isEqualTo(404);
            JsonNode errors = json.readTree(response.body()).get("errors");
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).get("code").asString()).isEqualTo("DISCOUNT_NOT_FOUND");
            assertThat(errors.get(0).has("field") && !errors.get(0).get("field").isNull()).isFalse();
        }
    }

    @Test
    void a_failure_of_the_store_answers_500_with_the_envelope_and_logs_the_exception_s_class_and_frames_not_its_message(
            CapturedOutput output) {
        String secret = "secret-" + UUID.randomUUID();
        doThrow(new DataAccessResourceFailureException("the database is down: " + secret))
                .when(discounts).find(any());

        HttpResponse<String> response = client.read(UUID.randomUUID().toString());

        assertThat(response.statusCode()).isEqualTo(500);
        JsonNode envelope = json.readTree(response.body());
        assertThat(envelope.get("errors")).isEmpty();
        assertThat(envelope.get("detail").asString()).isNotBlank();
        String errorId = envelope.get("errorId").asString();
        assertThat(output.getAll()).contains(errorId).contains("status=500")
                .contains(DataAccessResourceFailureException.class.getName())
                .contains("at com.commerce.discount.application.GetDiscountUseCase")
                .doesNotContain(secret);
        assertThat(response.body()).doesNotContain(secret);
    }

    @Test
    void refuses_a_missing_request_id_with_400_before_the_identifier_and_ignores_accept_language() {
        Map<String, String> headers = headers();
        headers.remove("X-Request-Id");

        HttpResponse<String> response = client.read("1-1-1-1-1", headers);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(json.readTree(response.body()).get("errors").get(0).get("field").asString())
                .isEqualTo("X-Request-Id");

        Map<String, String> french = headers();
        french.put("Accept-Language", "fr-FR");
        String discountId = json.readTree(client.create(discountRequest(40)).body()).get("discountId").asString();
        assertThat(client.read(discountId, french).statusCode()).isEqualTo(200);
    }
}
