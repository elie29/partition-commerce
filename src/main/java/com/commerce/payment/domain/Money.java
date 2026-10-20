package com.commerce.payment.domain;

/** An amount in minor units, with its currency, a three-letter code (INV-DATA-001). */
public record Money(long amountMinor, String currency) {

    public Money {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new InvalidPayment("INV-DATA-001: an amount is stored with a three-letter ISO 4217 code");
        }
    }

    /** The amount of a refund: a positive whole number of minor units, with its currency (BR-26). */
    public static Money ofRefund(long amountMinor, String currency) {
        if (amountMinor <= 0) {
            throw new InvalidPayment("BR-26: a refund amount is a positive whole number of minor units");
        }
        return new Money(amountMinor, currency);
    }
}
