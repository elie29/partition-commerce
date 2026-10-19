package com.commerce.pricelist.application;

import com.commerce.pricelist.domain.PriceList;
import java.util.Optional;
import java.util.UUID;

/** The price lists of the store, the port {@code price-list}'s outbound adapter implements. */
public interface PriceLists {

    void add(PriceList priceList);

    Optional<PriceList> find(UUID priceListId);
}
