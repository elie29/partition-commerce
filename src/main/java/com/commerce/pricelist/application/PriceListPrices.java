package com.commerce.pricelist.application;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * The contract {@code price-list} shows {@code product} in process (both specs, {@code Contracts with neighbours}):
 * the price of a product in a price list on a day, or none. An unknown list and no price that applies are not told
 * apart. Called only from {@code product}'s outbound adapter.
 */
public interface PriceListPrices {

    Optional<PriceListPrice> priceOn(UUID productId, UUID priceListId, LocalDate day);
}
