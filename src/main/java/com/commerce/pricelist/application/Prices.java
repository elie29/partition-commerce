package com.commerce.pricelist.application;

import com.commerce.pricelist.domain.Price;
import java.util.Optional;
import java.util.UUID;

/** The prices of the store, the port {@code price-list}'s outbound adapter implements. */
public interface Prices {

    /** Sets the one price of a product in a list (BR-1, P-5): the last to commit replaces the other. */
    void set(Price price);

    /** The one price the list holds for the product now, if any (BR-1: no history). */
    Optional<Price> find(UUID priceListId, UUID productId);
}
