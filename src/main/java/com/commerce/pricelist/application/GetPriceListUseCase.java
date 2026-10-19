package com.commerce.pricelist.application;

import org.springframework.stereotype.Service;

/** Reads a price list: BR-9 of the spec of {@code price-list}. */
@Service
public class GetPriceListUseCase {

    private final PriceLists priceLists;

    public GetPriceListUseCase(PriceLists priceLists) {
        this.priceLists = priceLists;
    }

    /** @throws PriceListNotFound when the identifier is not a UUID (AR-3) or names no list */
    public PriceListView get(String priceListId) {
        return Identifiers.parse(priceListId)
                .flatMap(priceLists::find)
                .map(PriceListView::of)
                .orElseThrow(PriceListNotFound::new);
    }
}
