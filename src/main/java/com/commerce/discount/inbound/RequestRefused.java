package com.commerce.discount.inbound;

import com.commerce.discount.application.Fault;
import java.util.List;

/** A 400 of {@code discount}: the fields at fault of the first group refused (AR-1). Carries no value sent. */
class RequestRefused extends RuntimeException {

    private final transient List<Fault> faults;

    RequestRefused(List<Fault> faults) {
        super("AR-1: the request is refused");
        this.faults = List.copyOf(faults);
    }

    List<Fault> faults() {
        return faults;
    }
}
