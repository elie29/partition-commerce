package com.commerce.product.outbound;

import com.commerce.product.application.ProductPage;
import com.commerce.product.application.ProductView;
import com.commerce.product.application.Products;
import com.commerce.product.application.ReferenceAlreadyHeld;
import com.commerce.product.domain.Product;
import com.commerce.product.domain.ProductStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * The products in the database, through {@link JdbcClient} (V7, V9); the unique constraint on the reference holds BR-1.
 * The reads filter nothing on the status (BR-9).
 */
@Repository
class JdbcProducts implements Products {

    private static final String COLUMNS = "product_id, reference, label, created_at, status";

    private final JdbcClient jdbc;

    JdbcProducts(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void add(Product product) {
        try {
            jdbc.sql("INSERT INTO product (" + COLUMNS + ") VALUES (:id, :reference, :label, :createdAt, :status)")
                    .param("id", product.productId())
                    .param("reference", product.reference())
                    .param("label", product.label())
                    .param("createdAt", product.createdAt().atOffset(ZoneOffset.UTC))
                    .param("status", product.status().name())
                    .update();
        } catch (DuplicateKeyException e) {
            throw new ReferenceAlreadyHeld();
        }
    }

    @Override
    public Optional<Product> find(UUID productId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM product WHERE product_id = :id")
                .param("id", productId)
                .query(JdbcProducts::product)
                .optional();
    }

    @Override
    public void changeStatus(Product product) {
        jdbc.sql("UPDATE product SET status = :status WHERE product_id = :id")
                .param("status", product.status().name())
                .param("id", product.productId())
                .update();
    }

    /** A page past the last reads no row: its offset may exceed what SQL takes, the totals are still counted. */
    @Override
    public ProductPage page(Collection<String> references, long page, int size) {
        String where = references.isEmpty() ? "" : " WHERE reference IN (:references)";
        JdbcClient.StatementSpec count = jdbc.sql("SELECT COUNT(*) FROM product" + where);
        if (!references.isEmpty()) {
            count = count.param("references", references);
        }
        long total = count.query(Long.class).single();
        long offset = page * size;
        if (offset >= total) {
            return new ProductPage(List.of(), total);
        }
        JdbcClient.StatementSpec select = jdbc.sql(
                        "SELECT " + COLUMNS + " FROM product" + where + " ORDER BY reference LIMIT :size OFFSET :offset")
                .param("size", size)
                .param("offset", offset);
        if (!references.isEmpty()) {
            select = select.param("references", references);
        }
        return new ProductPage(
                select.query(JdbcProducts::product).list().stream().map(ProductView::of).toList(), total);
    }

    private static Product product(ResultSet row, int rowNumber) throws SQLException {
        return new Product(
                row.getObject("product_id", UUID.class),
                row.getString("reference"),
                row.getString("label"),
                row.getObject("created_at", OffsetDateTime.class).toInstant(),
                ProductStatus.valueOf(row.getString("status")));
    }
}
