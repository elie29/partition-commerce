package com.commerce.refund.domain;

/**
 * The customer of an invoice, its {@code customerId}, bounded by AR-1: the one a fraud review names (AR-3). It appears
 * in no log and no error (BR-31): {@link #toString()} hides it.
 */
public record CustomerId(String value) {

    public CustomerId {
        Text.require(value, "AR-3: a customer under fraud review is bounded by AR-1");
    }

    @Override
    public String toString() {
        return "CustomerId[hidden]";
    }
}
