package com.commerce.product.application;

import com.commerce.product.domain.PriceList;
import java.util.Optional;
import java.util.UUID;

/** The price lists of the store, the port {@code price-list}'s outbound adapter implements. */
public interface PriceLists {

    void add(PriceList priceList);

    Optional<PriceList> find(UUID priceListId);
}
