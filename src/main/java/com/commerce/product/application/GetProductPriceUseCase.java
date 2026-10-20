package com.commerce.product.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Reads the price of a product in a price list on a day: BR-23 and BR-24 of the spec of {@code product}. The query
 * was checked before (content, BR-24); the price is read from the list's own data, {@code product} is not read
 * (BR-21).
 */
@Service
public class GetProductPriceUseCase {

    private final Prices prices;
    private final Clock clock;

    public GetProductPriceUseCase(Prices prices, Clock clock) {
        this.prices = prices;
        this.clock = clock;
    }

    /**
     * {@code at} absent is today, the day in UTC.
     *
     * @throws PriceNotFound when an identifier is not a UUID (AR-3), or the list holds no price that applies
     */
    public PriceView get(String priceListId, String productId, LocalDate at) {
        UUID list = Identifiers.parse(priceListId).orElseThrow(PriceNotFound::new);
        UUID product = Identifiers.parse(productId).orElseThrow(PriceNotFound::new);
        LocalDate day = at != null ? at : LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        return prices.find(list, product)
                .filter(price -> price.appliesOn(day))
                .map(PriceView::of)
                .orElseThrow(PriceNotFound::new);
    }
}
