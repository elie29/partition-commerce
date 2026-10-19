package com.commerce.payment.domain;

/** An amount in minor units, with its currency, a three-letter code (INV-DATA-001). */
public record Money(long amountMinor, String currency) {

    public Money {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new InvalidPayment("INV-DATA-001: an amount is stored with a three-letter ISO 4217 code");
        }
    }
}
