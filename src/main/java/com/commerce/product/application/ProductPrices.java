package com.commerce.product.application;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/** The price of a product in a price list on a day, or none: the port {@code product}'s outbound adapter implements. */
public interface ProductPrices {

    Optional<ProductPriceView> priceOn(UUID productId, UUID priceListId, LocalDate day);
}
