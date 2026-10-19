package com.commerce.invoice.domain;

import java.util.Currency;

/** An amount in minor units, with its currency, a known three-letter ISO 4217 code (BR-9). */
public record Money(long amountMinor, String currency) {

    public Money {
        if (currency == null || !currency.matches("[A-Z]{3}") || !isKnown(currency)) {
            throw new InvalidInvoice("BR-9: a currency is a known three-letter ISO 4217 code");
        }
    }

    /** The sum of two amounts of one currency (BR-7), within the range of the amount (BR-10). */
    public Money plus(Money other) {
        if (!currency.equals(other.currency)) {
            throw new InvalidInvoice("BR-7: the lines of an invoice are all in one currency");
        }
        try {
            return new Money(Math.addExact(amountMinor, other.amountMinor), currency);
        } catch (ArithmeticException e) {
            throw new InvalidInvoice("BR-10: the amount stays within the range of the amount");
        }
    }

    boolean isNegative() {
        return amountMinor < 0;
    }

    private static boolean isKnown(String code) {
        return Currency.getAvailableCurrencies().stream().anyMatch(c -> c.getCurrencyCode().equals(code));
    }
}
