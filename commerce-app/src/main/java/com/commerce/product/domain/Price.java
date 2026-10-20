package com.commerce.product.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

/** The price of a product in a price list: an amount in {@link Money} and its {@code effectiveFrom}, an instant in UTC. */
public record Price(UUID priceListId, UUID productId, Money amount, Instant effectiveFrom) {

    public Price {
        Objects.requireNonNull(priceListId);
        Objects.requireNonNull(productId);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(effectiveFrom);
    }

    /**
     * Applies (`docs/specs/catalog/product/spec.md`): on a day, its {@code effectiveFrom} is on or before the end of that day,
     * in UTC; a day ends before 00:00 UTC of the next day.
     */
    public boolean appliesOn(LocalDate day) {
        return effectiveFrom.isBefore(day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}
