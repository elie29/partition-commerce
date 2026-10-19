package com.commerce.pricelist.domain;

/** An amount in minor units, with its currency (INV-DATA-001); the amount of a price is never negative (BR-2). */
public record Money(long amountMinor, Currency currency) {

    public Money {
        if (!isAmount(amountMinor)) {
            throw new InvalidPriceList("BR-2: the amount of a price is never negative");
        }
        if (currency == null) {
            throw new InvalidPriceList("INV-DATA-001: an amount has its currency");
        }
    }

    /** Zero is a price (BR-2). */
    public static boolean isAmount(long amountMinor) {
        return amountMinor >= 0;
    }

    /** Hides the amount: {@code price-list} never logs an amount (AR-2). */
    @Override
    public String toString() {
        return "Money[hidden]";
    }
}
