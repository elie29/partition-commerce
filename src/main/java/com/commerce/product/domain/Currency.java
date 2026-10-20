package com.commerce.product.domain;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BR-4: a currency is a known ISO 4217 code, a code of the JDK's {@code Currency} list that does not start with
 * {@code X}; the list is the JDK's, updated with the JDK, fund codes and withdrawn codes it still lists included.
 * Checked on incoming requests only ({@link #of}); a stored code is read as kept ({@link #stored}), a code a later JDK
 * drops included (BA, architect). Written for {@code price-list}, not imported from {@code invoice}.
 */
public final class Currency {

    private static final Set<String> KNOWN = java.util.Currency.getAvailableCurrencies().stream()
            .map(java.util.Currency::getCurrencyCode)
            .filter(code -> !code.startsWith("X"))
            .collect(Collectors.toUnmodifiableSet());

    private final String code;

    private Currency(String code) {
        this.code = Objects.requireNonNull(code);
    }

    /** A code a request brings: refused unless known (BR-4). */
    public static Currency of(String code) {
        if (!isKnown(code)) {
            throw new InvalidPriceList("BR-4: a currency is a known ISO 4217 code");
        }
        return new Currency(code);
    }

    /** A code read back from the store, as it was kept: BR-4 is not checked again. */
    public static Currency stored(String code) {
        return new Currency(code);
    }

    public static boolean isKnown(String code) {
        return code != null && KNOWN.contains(code);
    }

    public String code() {
        return code;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Currency currency && code.equals(currency.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code;
    }
}
