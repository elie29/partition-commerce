package com.commerce.pricelist.application;

import java.time.Instant;

/** What {@link PriceListPrices} answers: the amount, the currency and the {@code effectiveFrom} of the price. */
public record PriceListPrice(long amountMinor, String currency, Instant effectiveFrom) {

    /** The amount is never written to a log (AR-2). */
    @Override
    public String toString() {
        return "PriceListPrice[hidden]";
    }
}
