package com.commerce.product.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * A price list: the prices of products in one currency, for one channel. It holds no collection of prices: one price
 * per product is held by the store's key (BR-1, P-5); it makes each price, in its own currency (BR-3).
 */
public record PriceList(UUID id, String name, Currency currency, Channel channel) {

    public PriceList {
        if (id == null || currency == null || channel == null) {
            throw new InvalidPriceList("a price list has its identifier, its currency and its channel");
        }
        if (name == null || isBlankName(name)) {
            throw new InvalidPriceList("BR-5: a name is not blank");
        }
    }

    /** Blank (`docs/specs/catalog/area.md`): empty or only white space, non-breaking spaces included. */
    public static boolean isBlankName(String name) {
        return name.codePoints().allMatch(c -> Character.isWhitespace(c) || Character.isSpaceChar(c));
    }

    public boolean isInItsCurrency(Currency other) {
        return currency.equals(other);
    }

    /** The price of a product in this list (BR-3: in the list's currency), effective from the instant given. */
    public Price price(UUID productId, Money amount, Instant effectiveFrom) {
        if (!isInItsCurrency(amount.currency())) {
            throw new InvalidPriceList("BR-3: a price is in its price list's currency");
        }
        return new Price(id, productId, amount, effectiveFrom);
    }

    /** The name is never written to a log (AR-2). */
    @Override
    public String toString() {
        return "PriceList[" + id + "]";
    }
}
