package com.commerce.product.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Reads the price of a product in a price list on a day: BR-9 and BR-10 of the spec of {@code product}. The query was
 * checked before (content, BR-10); the product is read, then the price, through {@link ProductPrices}.
 */
@Service
public class GetProductPriceUseCase {

    private final Products products;
    private final ProductPrices prices;
    private final Clock clock;

    public GetProductPriceUseCase(Products products, ProductPrices prices, Clock clock) {
        this.products = products;
        this.prices = prices;
        this.clock = clock;
    }

    /**
     * {@code at} absent is today, the day in UTC.
     *
     * @throws PriceNotFound when the product, the price list or a price that applies is missing
     */
    public ProductPriceView get(String productId, String priceListId, LocalDate at) {
        UUID product = Identifiers.parse(productId)
                .filter(id -> products.find(id).isPresent())
                .orElseThrow(PriceNotFound::new);
        UUID list = Identifiers.parse(priceListId).orElseThrow(PriceNotFound::new);
        LocalDate day = at != null ? at : LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        return prices.priceOn(product, list, day).orElseThrow(PriceNotFound::new);
    }
}
