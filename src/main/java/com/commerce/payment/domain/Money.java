package com.commerce.payment.domain;

/** An amount in minor units, with its currency, a three-letter code (AR-2). */
public record Money(long amountMinor, String currency) {

    public Money {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new InvalidPayment("AR-2: an amount is stored with a three-letter ISO 4217 code");
        }
    }
}
