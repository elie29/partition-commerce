package com.commerce.product.application;

import com.commerce.product.domain.Channel;
import com.commerce.product.domain.Currency;
import com.commerce.product.domain.PriceList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Creates a price list: BR-4 to BR-7 of the spec of {@code price-list}; it never answers 409 (BR-7). */
@Service
public class CreatePriceListUseCase {

    private final PriceLists priceLists;

    public CreatePriceListUseCase(PriceLists priceLists) {
        this.priceLists = priceLists;
    }

    /**
     * The faults of the content the domain judges, beyond the contract's schema: a blank name (BR-5), a currency that
     * is not a known code (BR-4). A value of the wrong type is passed as {@code null} and judged by the schema only.
     */
    public List<Fault> contentFaults(String name, String currency) {
        List<Fault> faults = new ArrayList<>();
        if (name != null && PriceList.isBlankName(name)) {
            faults.add(new Fault("name", "The name is blank."));
        }
        if (currency != null && !Currency.isKnown(currency)) {
            faults.add(new Fault("currency", "The currency is not a known ISO 4217 code."));
        }
        return faults;
    }

    /** A content {@link #contentFaults} and the contract accept; the validity is accepted, not kept (BR-6). */
    public PriceListView create(String name, String currency, String channel) {
        PriceList created = new PriceList(UUID.randomUUID(), name, Currency.of(currency), Channel.valueOf(channel));
        priceLists.add(created);
        return PriceListView.of(created);
    }
}
