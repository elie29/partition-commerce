package com.commerce.discount.application;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * BR-4: a currency is a known ISO 4217 code, as {@code price-list}'s BR-4 says: a code of the JDK's {@code Currency}
 * list that does not start with {@code X}; the list is the JDK's, updated with the JDK. Checked on incoming requests
 * only; a stored code is read as kept. Written for {@code discount}, not imported (each spec its own check).
 */
final class KnownCurrency {

    private static final Set<String> KNOWN = java.util.Currency.getAvailableCurrencies().stream()
            .map(java.util.Currency::getCurrencyCode)
            .filter(code -> !code.startsWith("X"))
            .collect(Collectors.toUnmodifiableSet());

    private KnownCurrency() {
    }

    static boolean is(String code) {
        return code != null && KNOWN.contains(code);
    }
}
