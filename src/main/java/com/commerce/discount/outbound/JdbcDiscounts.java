package com.commerce.discount.outbound;

import com.commerce.discount.application.DiscountView;
import com.commerce.discount.application.Discounts;
import com.commerce.discount.application.MinimumOrder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The discounts in the database, through {@link JdbcClient} (V8). */
@Repository
class JdbcDiscounts implements Discounts {

    private final JdbcClient jdbc;

    JdbcDiscounts(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void add(DiscountView discount) {
        MinimumOrder minimumOrder = discount.minimumOrder();
        jdbc.sql("INSERT INTO discount (discount_id, percent, minimum_order_minor, minimum_order_currency) "
                        + "VALUES (:id, :percent, :minor, :currency)")
                .param("id", discount.discountId())
                .param("percent", discount.percent())
                .param("minor", minimumOrder == null ? null : minimumOrder.amountMinor())
                .param("currency", minimumOrder == null ? null : minimumOrder.currency())
                .update();
    }

    /** A stored currency is read as kept: BR-4 is checked on incoming requests only. */
    @Override
    public Optional<DiscountView> find(UUID discountId) {
        return jdbc.sql("SELECT discount_id, percent, minimum_order_minor, minimum_order_currency FROM discount "
                        + "WHERE discount_id = :id")
                .param("id", discountId)
                .query((row, n) -> {
                    String currency = row.getString("minimum_order_currency");
                    return new DiscountView(
                            row.getObject("discount_id", UUID.class),
                            row.getInt("percent"),
                            currency == null ? null : new MinimumOrder(row.getLong("minimum_order_minor"), currency));
                })
                .optional();
    }
}
