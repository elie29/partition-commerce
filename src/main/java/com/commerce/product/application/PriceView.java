package com.commerce.product.application;

import com.commerce.product.domain.Price;
import java.time.Instant;
import java.util.UUID;

/** A price as the operations answer it: the product, the amount, its currency, its {@code effectiveFrom} in UTC. */
public record PriceView(UUID productId, long amountMinor, String currency, Instant effectiveFrom) {

    static PriceView of(Price price) {
        return new PriceView(price.productId(), price.amount().amountMinor(), price.amount().currency().code(),
                price.effectiveFrom());
    }

    /** The amount is never written to a log (AR-2). */
    @Override
    public String toString() {
        return "PriceView[" + productId + "]";
    }
}
