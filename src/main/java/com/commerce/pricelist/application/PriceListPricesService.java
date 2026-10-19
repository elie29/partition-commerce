package com.commerce.pricelist.application;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * {@link PriceListPrices}: the one price the list holds now (BR-1, no history), when it applies on the day (Applies,
 * {@code docs/specs/catalog/area.md}); a future price hides the price until its day.
 */
@Service
class PriceListPricesService implements PriceListPrices {

    private final Prices prices;

    PriceListPricesService(Prices prices) {
        this.prices = prices;
    }

    @Override
    public Optional<PriceListPrice> priceOn(UUID productId, UUID priceListId, LocalDate day) {
        return prices.find(priceListId, productId)
                .filter(price -> price.appliesOn(day))
                .map(price -> new PriceListPrice(
                        price.amount().amountMinor(), price.amount().currency().code(), price.effectiveFrom()));
    }
}
