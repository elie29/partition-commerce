package com.commerce.product.outbound;

import com.commerce.product.application.PriceLists;
import com.commerce.product.domain.Channel;
import com.commerce.product.domain.Currency;
import com.commerce.product.domain.PriceList;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The price lists in the database, through {@link JdbcClient} (V7); a stored currency is read as kept (BR-4). */
@Repository
class JdbcPriceLists implements PriceLists {

    private final JdbcClient jdbc;

    JdbcPriceLists(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void add(PriceList priceList) {
        jdbc.sql("INSERT INTO price_list (price_list_id, name, currency, channel) VALUES (:id, :name, :currency, :channel)")
                .param("id", priceList.id())
                .param("name", priceList.name())
                .param("currency", priceList.currency().code())
                .param("channel", priceList.channel().name())
                .update();
    }

    @Override
    public Optional<PriceList> find(UUID priceListId) {
        return jdbc.sql("SELECT price_list_id, name, currency, channel FROM price_list WHERE price_list_id = :id")
                .param("id", priceListId)
                .query((row, n) -> new PriceList(
                        row.getObject("price_list_id", UUID.class),
                        row.getString("name"),
                        Currency.stored(row.getString("currency")),
                        Channel.valueOf(row.getString("channel"))))
                .optional();
    }
}
