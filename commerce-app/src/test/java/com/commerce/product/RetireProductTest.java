package com.commerce.product;

import static com.commerce.product.ProductClient.freshReference;
import static com.commerce.product.ProductClient.productRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.commerce.product.application.ProductNotFound;
import com.commerce.product.application.Products;
import com.commerce.product.application.RetireProductUseCase;
import com.commerce.product.domain.Product;
import com.commerce.product.domain.ProductStatus;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.json.JsonMapper;

/**
 * The use case "retire a product": BR-9 of the spec of {@code product}, with BR-1 and BR-8 as BR-9 names them, and the
 * {@code Product} aggregate (P-6). Only its tests reach the use case in this version: no operation exposes it.
 */
@ProductApiTest
@ExtendWith(OutputCaptureExtension.class)
class RetireProductTest {

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    RetireProductUseCase retireProduct;

    /** Spied on: a test checks what the use case writes. */
    @MockitoSpyBean
    Products products;

    ProductClient client;

    @BeforeEach
    void client() {
        client = new ProductClient(port);
    }

    @Test
    void a_new_product_is_active() {
        UUID productId = created(freshReference(), "Book");

        assertThat(products.find(productId)).get().extracting(Product::isRetired).isEqualTo(false);
        assertThat(statusOf(productId)).isEqualTo("ACTIVE");
    }

    @Test
    void retiring_an_active_product_makes_it_retired_in_the_store() {
        UUID productId = created(freshReference(), "Book");

        retireProduct.retire(productId);

        assertThat(products.find(productId)).get().extracting(Product::isRetired).isEqualTo(true);
        assertThat(statusOf(productId)).isEqualTo("RETIRED");
    }

    @Test
    void retiring_a_retired_product_leaves_it_retired_and_writes_nothing() {
        UUID productId = created(freshReference(), "Book");
        retireProduct.retire(productId);
        clearInvocations(products);

        retireProduct.retire(productId);

        verify(products, never()).changeStatus(any());
        assertThat(statusOf(productId)).isEqualTo("RETIRED");
    }

    @Test
    void retiring_a_product_that_does_not_exist_is_refused_as_not_found_and_nothing_changes() {
        long retiredBefore = retiredCount();
        clearInvocations(products);

        assertThatThrownBy(() -> retireProduct.retire(UUID.randomUUID())).isInstanceOf(ProductNotFound.class);

        verify(products, never()).changeStatus(any());
        assertThat(retiredCount()).isEqualTo(retiredBefore);
    }

    @Test
    void a_retired_product_keeps_its_reference_which_stays_taken() {
        String reference = freshReference();
        UUID productId = created(reference, "Book");

        retireProduct.retire(productId);

        assertThat(client.create(productRequest(reference, "Another book")).statusCode()).isEqualTo(409);
    }

    @Test
    void reads_answer_a_retired_product_as_they_did_before_its_retirement() {
        String reference = freshReference();
        UUID productId = created(reference, "Book");
        String readBefore = client.read(productId.toString()).body();
        String listBefore = client.get("/products?references=" + reference).body();

        retireProduct.retire(productId);

        assertThat(json.readTree(client.read(productId.toString()).body())).isEqualTo(json.readTree(readBefore));
        assertThat(json.readTree(client.get("/products?references=" + reference).body()))
                .isEqualTo(json.readTree(listBefore));
    }

    @Test
    void a_product_stored_before_its_status_existed_is_active() {
        UUID productId = UUID.randomUUID();
        jdbc.sql("INSERT INTO product (product_id, reference, label, created_at)"
                        + " VALUES (:id, :reference, :label, :createdAt)")
                .param("id", productId)
                .param("reference", freshReference())
                .param("label", "Book")
                .param("createdAt", OffsetDateTime.now(ZoneOffset.UTC))
                .update();

        assertThat(products.find(productId)).get().extracting(Product::isRetired).isEqualTo(false);
        assertThat(statusOf(productId)).isEqualTo("ACTIVE");
    }

    @Test
    void a_product_is_created_active_retired_once_and_never_made_active_again() {
        Product product = Product.create(UUID.randomUUID(), "BOK-000123", "Book", Instant.now());

        assertThat(product.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.retire().status()).isEqualTo(ProductStatus.RETIRED);
        assertThat(product.retire().retire()).isEqualTo(product.retire());
    }

    @Test
    void no_method_of_a_product_but_create_and_retire_makes_a_product_and_none_sets_its_status() {
        assertThat(Arrays.stream(Product.class.getDeclaredMethods())
                        .filter(m -> Modifier.isPublic(m.getModifiers()))
                        .filter(m -> m.getReturnType().equals(Product.class))
                        .map(Method::getName))
                .containsExactlyInAnyOrder("create", "retire");
        assertThat(Arrays.stream(Product.class.getDeclaredMethods()).map(Method::getName))
                .noneMatch(name -> name.startsWith("set") || name.startsWith("with") || name.equals("reactivate"));
    }

    @Test
    void the_retirement_logs_neither_the_label_nor_the_reference(CapturedOutput output) {
        String reference = freshReference();
        String label = "Label-" + UUID.randomUUID();
        UUID productId = created(reference, label);

        retireProduct.retire(productId);

        assertThat(output.getAll()).doesNotContain(label).doesNotContain(reference);
    }

    private UUID created(String reference, String label) {
        return UUID.fromString(json.readTree(client.create(productRequest(reference, label)).body())
                .get("productId").asString());
    }

    private String statusOf(UUID productId) {
        return jdbc.sql("SELECT status FROM product WHERE product_id = :id")
                .param("id", productId)
                .query(String.class)
                .single();
    }

    private long retiredCount() {
        return jdbc.sql("SELECT COUNT(*) FROM product WHERE status = 'RETIRED'").query(Long.class).single();
    }
}
