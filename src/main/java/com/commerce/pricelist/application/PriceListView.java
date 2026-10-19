package com.commerce.pricelist.application;

import com.commerce.pricelist.domain.PriceList;
import java.util.UUID;

/** A price list as the operations answer it: its identifier, its name, its currency, its channel (BR-6). */
public record PriceListView(UUID priceListId, String name, String currency, String channel) {

    static PriceListView of(PriceList list) {
        return new PriceListView(list.id(), list.name(), list.currency().code(), list.channel().name());
    }

    /** The name is never written to a log (AR-2). */
    @Override
    public String toString() {
        return "PriceListView[" + priceListId + "]";
    }
}
