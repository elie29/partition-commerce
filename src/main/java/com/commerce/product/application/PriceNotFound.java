package com.commerce.product.application;

/** BR-9: no such product, no such price list, or no price that applies: one 404. Carries no value of the request. */
public class PriceNotFound extends RuntimeException {

    public PriceNotFound() {
        super("BR-9: no such product, or no price for it in that price list");
    }
}
