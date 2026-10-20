package com.commerce.product.outbound;

import com.commerce.product.application.Prices;
import com.commerce.product.domain.Currency;
import com.commerce.product.domain.Money;
import com.commerce.product.domain.Price;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * The prices in the database, through {@link JdbcClient} (V7). One price per product and list is held by the key
 * {@code (price_list_id, product_id)} and one {@code MERGE}: the last to commit replaces, and two first prices at the
 * same moment both succeed (BR-10, P-5).
 */
@Repository
class JdbcPrices implements Prices {

    private final JdbcClient jdbc;

    JdbcPrices(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void set(Price price) {
        jdbc.sql("MERGE INTO price (price_list_id, product_id, amount_minor, amount_currency, effective_from)"
                        + " KEY (price_list_id, product_id) VALUES (:list, :product, :amount, :currency, :from)")
                .param("list", price.priceListId())
                .param("product", price.productId())
                .param("amount", price.amount().amountMinor())
                .param("currency", price.amount().currency().code())
                .param("from", price.effectiveFrom().atOffset(ZoneOffset.UTC))
                .update();
    }

    @Override
    public Optional<Price> find(UUID priceListId, UUID productId) {
        return jdbc.sql("SELECT amount_minor, amount_currency, effective_from FROM price"
                        + " WHERE price_list_id = :list AND product_id = :product")
                .param("list", priceListId)
                .param("product", productId)
                .query((row, n) -> new Price(
                        priceListId,
                        productId,
                        new Money(row.getLong("amount_minor"), Currency.stored(row.getString("amount_currency"))),
                        row.getObject("effective_from", OffsetDateTime.class).toInstant()))
                .optional();
    }
}
