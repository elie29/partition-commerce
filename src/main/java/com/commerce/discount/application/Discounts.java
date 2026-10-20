package com.commerce.discount.application;

import java.util.Optional;
import java.util.UUID;

/** The discounts of the store, the port {@code discount}'s outbound adapter implements. */
public interface Discounts {

    void add(DiscountView discount);

    Optional<DiscountView> find(UUID discountId);
}
