package com.commerce.product;

import static com.commerce.product.PriceListClient.priceListRequest;
import static com.commerce.product.PriceListClient.priceRequest;
import static org.assertj.core.api.Assertions.assertThat;

import com.commerce.product.application.SetPriceUseCase;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
 * The use case "set the price of a product in a price list": BR-1 to BR-4, BR-8 to BR-10 and BR-12 of the spec of
 * {@code price-list}, AR-1 to AR-5 and P-5.
 */
@PriceListApiTest
@ExtendWith(OutputCaptureExtension.class)
class SetPriceTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    /** Reads the prices a list holds. */
    @Autowired
    JdbcClient jdbc;

    /** Called from two threads at once, without HTTP, for the many rounds of P-5. */
    @Autowired
    SetPriceUseCase setPrice;

    PriceListClient client;

    @BeforeEach
    void client() {
        client = new PriceListClient(port);
    }

    @Test
    void sets_a_price_answering_200_effective_now_in_utc_to_the_millisecond_when_effective_from_is_absent() {
        String listId = list("EUR");
        String productId = UUID.randomUUID().toString();
        OffsetDateTime before = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        HttpResponse<String> set = client.setPrice(listId, productId, priceRequest(1999, "EUR", null));

        assertThat(set.statusCode()).isEqualTo(200);
        JsonNode price = json.readTree(set.body());
        assertThat(price.get("productId").asString()).isEqualTo(productId);
        assertThat(price.get("price").get("amountMinor").asLong()).isEqualTo(1999);
        assertThat(price.get("price").get("currency").asString()).isEqualTo("EUR");
        OffsetDateTime effectiveFrom = OffsetDateTime.parse(price.get("effectiveFrom").asString());
        assertThat(effectiveFrom).isAfterOrEqualTo(before);
        assertThat(effectiveFrom.getNano() % 1_000_000).isZero();
        assertThat(effectiveFrom.getOffset().getTotalSeconds()).isZero();
    }

    @Test
    void reads_an_effective_from_with_an_offset_in_utc_and_accepts_one_in_the_past() {
        String listId = list("EUR");

        HttpResponse<String> set = client.setPrice(listId, UUID.randomUUID().toString(),
                priceRequest(0, "EUR", "2020-01-01T10:00:00.123456+02:00"));

        assertThat(set.statusCode()).isEqualTo(200);
        assertThat(OffsetDateTime.parse(json.readTree(set.body()).get("effectiveFrom").asString()))
                .isEqualTo(OffsetDateTime.parse("2020-01-01T08:00:00.123Z"));
    }

    @Test
    void a_second_price_replaces_the_first_and_the_list_keeps_no_history() {
        String listId = list("EUR");
        String productId = UUID.randomUUID().toString();
        assertThat(client.setPrice(listId, productId, priceRequest(100, "EUR", null)).statusCode()).isEqualTo(200);

        HttpResponse<String> again = client.setPrice(listId, productId, priceRequest(250, "EUR", null));

        assertThat(again.statusCode()).isEqualTo(200);
        assertThat(pricesOf(listId, productId)).containsExactly(250L);
    }

    @Test
    void of_two_prices_set_at_the_same_moment_both_answer_200_and_one_is_kept() throws Exception {
        for (int round = 0; round < 5; round++) {
            String listId = list("EUR");
            String productId = UUID.randomUUID().toString();
            CountDownLatch start = new CountDownLatch(1);
            List<Callable<Integer>> calls = new ArrayList<>();
            for (long amount : new long[] {100, 200}) {
                calls.add(() -> {
                    start.await();
                    return client.setPrice(listId, productId, priceRequest(amount, "EUR", null)).statusCode();
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

            assertThat(statuses).containsExactly(200, 200);
            assertThat(pricesOf(listId, productId)).hasSize(1).first().isIn(100L, 200L);
        }
    }

    /** P-5 under load: the race window of a first price is short, so many rounds, through the use case itself. */
    @Test
    void of_two_first_prices_set_at_the_same_moment_none_fails_over_many_rounds() throws Exception {
        String listId = list("EUR");
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            for (int round = 0; round < 500; round++) {
                String productId = UUID.randomUUID().toString();
                CountDownLatch start = new CountDownLatch(1);
                List<Future<?>> futures = new ArrayList<>();
                for (long amount : new long[] {100, 200}) {
                    futures.add(pool.submit(() -> {
                        start.await();
                        return setPrice.set(listId, productId, amount, "EUR", null);
                    }));
                }
                start.countDown();
                for (Future<?> future : futures) {
                    future.get();
                }
                assertThat(pricesOf(listId, productId)).as("round " + round).hasSize(1).first().isIn(100L, 200L);
            }
        }
    }

    @Test
    void prices_a_product_identifier_whether_product_holds_it_or_not() {
        String listId = list("EUR");
        String productId = UUID.randomUUID().toString();

        assertThat(client.setPrice(listId, productId, priceRequest(500, "EUR", null)).statusCode()).isEqualTo(200);
        assertThat(pricesOf(listId, productId)).containsExactly(500L);
    }

    @Test
    void refuses_a_negative_amount_with_400_and_zero_is_a_price() {
        String listId = list("EUR");
        String productId = UUID.randomUUID().toString();

        assertThat(fields(client.setPrice(listId, productId, priceRequest(-1, "EUR", null))))
                .containsExactly("/price/amountMinor");
        assertThat(pricesOf(listId, productId)).isEmpty();
        assertThat(client.setPrice(listId, productId, priceRequest(0, "EUR", null)).statusCode()).isEqualTo(200);
    }

    @Test
    void refuses_an_unknown_currency_a_currency_other_than_the_list_s_and_an_effective_from_without_offset() {
        String listId = list("EUR");
        String productId = UUID.randomUUID().toString();

        assertThat(fields(client.setPrice(listId, productId, priceRequest(100, "XYZ", null))))
                .containsExactly("/price/currency");
        assertThat(fields(client.setPrice(listId, productId, priceRequest(100, "USD", null))))
                .containsExactly("/price/currency");
        assertThat(fields(client.setPrice(listId, productId, priceRequest(100, "EUR", "2026-10-20T10:00:00"))))
                .containsExactly("/effectiveFrom");
        assertThat(pricesOf(listId, productId)).isEmpty();
    }

    @Test
    void a_negative_amount_with_an_unknown_currency_gives_two_items_with_another_currency_than_the_list_s_one() {
        String listId = list("EUR");
        String productId = UUID.randomUUID().toString();

        assertThat(fields(client.setPrice(listId, productId, priceRequest(-1, "XYZ", null))))
                .containsExactlyInAnyOrder("/price/amountMinor", "/price/currency");
        assertThat(fields(client.setPrice(listId, productId, priceRequest(-1, "USD", null))))
                .containsExactly("/price/amountMinor");
    }

    @Test
    void checks_the_body_then_the_list_then_the_product_then_the_list_s_currency() {
        String listId = list("EUR");
        String unknownList = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();

        assertThat(fields(client.setPrice(unknownList, "not-a-uuid", priceRequest(-1, "EUR", null))))
                .containsExactly("/price/amountMinor");
        assertNotFound(client.setPrice(unknownList, productId, priceRequest(100, "USD", null)));
        assertNotFound(client.setPrice("1-1-1-1-1", productId, priceRequest(100, "EUR", null)));
        assertNotFound(client.setPrice(listId, "1-1-1-1-1", priceRequest(100, "USD", null)));
        assertNotFound(client.setPrice(listId, "not-a-uuid", priceRequest(100, "EUR", null)));
    }

    @Test
    void refuses_what_the_contract_refuses_in_the_body_with_one_item_per_field_and_no_body_with_no_field() {
        String listId = list("EUR");
        String productId = UUID.randomUUID().toString();

        assertThat(fields(client.setPrice(listId, productId, "{}"))).containsExactly("/price");
        assertThat(fields(client.setPrice(listId, productId, "{\"price\":{\"amountMinor\":1.5}}")))
                .containsExactlyInAnyOrder("/price/amountMinor", "/price/currency");
        HttpResponse<String> none = client.setPrice(listId, productId, null);
        assertThat(none.statusCode()).isEqualTo(400);
        JsonNode item = json.readTree(none.body()).get("errors").get(0);
        assertThat(item.has("field") && !item.get("field").isNull()).isFalse();
    }

    @Test
    void no_output_and_no_error_answer_names_an_amount(CapturedOutput output) {
        String listId = list("EUR");
        long amount = 7_654_321_987L;

        HttpResponse<String> set = client.setPrice(listId, UUID.randomUUID().toString(),
                priceRequest(amount, "EUR", null));
        HttpResponse<String> refused = client.setPrice(listId, UUID.randomUUID().toString(),
                priceRequest(amount, "USD", null));

        assertThat(set.statusCode()).isEqualTo(200);
        assertThat(refused.statusCode()).isEqualTo(400);
        assertThat(refused.body()).doesNotContain(String.valueOf(amount));
        assertThat(output.getAll()).doesNotContain(String.valueOf(amount));
    }

    private String list(String currency) {
        HttpResponse<String> created = client.create(priceListRequest("Shop", currency, "SHOP"));
        assertThat(created.statusCode()).isEqualTo(201);
        return json.readTree(created.body()).get("priceListId").asString();
    }

    private void assertNotFound(HttpResponse<String> response) {
        assertThat(response.statusCode()).isEqualTo(404);
        JsonNode errors = json.readTree(response.body()).get("errors");
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).get("code").asString()).isEqualTo("PRICE_LIST_NOT_FOUND");
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

    private List<Long> pricesOf(String listId, String productId) {
        return jdbc.sql("SELECT amount_minor FROM price WHERE price_list_id = ? AND product_id = ?")
                .params(UUID.fromString(listId), UUID.fromString(productId))
                .query(Long.class)
                .list();
    }
}
