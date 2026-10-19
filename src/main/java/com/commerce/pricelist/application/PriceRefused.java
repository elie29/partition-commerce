package com.commerce.pricelist.application;

import java.util.List;

/**
 * A 400 on what the list holds, checked after its identifiers (BR-10): a price in a currency other than its list's
 * (BR-3). Carries the field at fault, never the value sent.
 */
public class PriceRefused extends RuntimeException {

    private final transient List<Fault> faults;

    public PriceRefused(List<Fault> faults) {
        super("BR-3: a price is in its price list's currency");
        this.faults = List.copyOf(faults);
    }

    public List<Fault> faults() {
        return faults;
    }
}
