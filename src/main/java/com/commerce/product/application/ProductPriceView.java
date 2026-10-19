package com.commerce.product.application;

import java.time.Instant;

/** The price of a product as {@code getProductPrice} answers it: the amount, its currency, its {@code effectiveFrom}. */
public record ProductPriceView(long amountMinor, String currency, Instant effectiveFrom) {

    /** The amount is never written to a log (AR-2). */
    @Override
    public String toString() {
        return "ProductPriceView[hidden]";
    }
}
